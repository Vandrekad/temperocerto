package br.edu.orlandovandres.temperocerto.data.repository

import br.edu.orlandovandres.temperocerto.data.local.LocalSeedData
import br.edu.orlandovandres.temperocerto.data.local.dao.IngredientCatalogDao
import br.edu.orlandovandres.temperocerto.data.local.dao.RecipeDao
import br.edu.orlandovandres.temperocerto.data.mapper.IngredientMapper
import br.edu.orlandovandres.temperocerto.data.mapper.RecipeMapper
import br.edu.orlandovandres.temperocerto.data.remote.api.TheMealDbApi
import br.edu.orlandovandres.temperocerto.di.IoDispatcher
import br.edu.orlandovandres.temperocerto.domain.model.CatalogIngredient
import br.edu.orlandovandres.temperocerto.domain.model.Recipe
import br.edu.orlandovandres.temperocerto.domain.model.SyncSummary
import br.edu.orlandovandres.temperocerto.domain.repository.ConnectivityObserver
import br.edu.orlandovandres.temperocerto.domain.repository.RecipeRepository
import com.google.gson.Gson
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repositório Offline First utilizando Room como Single Source of Truth (SSOT)
 * e TheMealDB API v1 como fonte de atualização incremental.
 */
@Singleton
class OfflineFirstRecipeRepository @Inject constructor(
    private val api: TheMealDbApi,
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientCatalogDao,
    private val recipeMapper: RecipeMapper,
    private val ingredientMapper: IngredientMapper,
    private val connectivityObserver: ConnectivityObserver,
    private val gson: Gson,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RecipeRepository {

    override fun observeRecipes(): Flow<List<Recipe>> =
        recipeDao.observeAllRecipes().map { entities ->
            entities.map(recipeMapper::entityToDomain)
        }

    override fun observeFavoriteRecipes(): Flow<List<Recipe>> =
        recipeDao.observeFavoriteRecipes().map { entities ->
            entities.map(recipeMapper::entityToDomain)
        }

    override fun observeRecipeById(id: String): Flow<Recipe?> =
        recipeDao.observeRecipeById(id).map { entity ->
            entity?.let(recipeMapper::entityToDomain)
        }

    override fun observeIngredientCatalog(): Flow<List<CatalogIngredient>> =
        ingredientDao.observeAllIngredients().map { entities ->
            entities.map(ingredientMapper::entityToDomain)
        }

    override suspend fun syncCatalog(force: Boolean): Result<SyncSummary> =
        withContext(ioDispatcher) {
            try {
                ensureSeeded()

                if (!connectivityObserver.currentIsOnline()) {
                    return@withContext Result.success(
                        SyncSummary(recipesSynced = 0, ingredientsSynced = 0, fromCacheOnly = true)
                    )
                }

                val now = System.currentTimeMillis()
                val latestCached = ingredientDao.getLatestCachedAt()
                val isFresh = latestCached != null &&
                    (now - latestCached) < SYNC_TTL_MILLIS &&
                    recipeDao.countRecipes() > MIN_ONLINE_RECIPE_COUNT

                if (!force && isFresh) {
                    return@withContext Result.success(
                        SyncSummary(recipesSynced = 0, ingredientsSynced = 0, fromCacheOnly = true)
                    )
                }

                var recipesSynced = 0
                var ingredientsSynced = 0
                var anyNetworkError: Throwable? = null

                coroutineScope {
                    val ingredientsDeferred = async {
                        runCatchingNonCancellation { api.listIngredients() }
                    }
                    val recipeLetters = listOf("c", "b", "p", "s")
                    val mealsDeferred = recipeLetters.map { letter ->
                        async { runCatchingNonCancellation { api.searchMealsByFirstLetter(letter) } }
                    }

                    val ingResult = ingredientsDeferred.await()
                    ingResult.onSuccess { response ->
                        val entities = response.meals.orEmpty()
                            .mapNotNull { ingredientMapper.dtoToEntity(it, now) }
                        if (entities.isNotEmpty()) {
                            ingredientDao.upsertAll(entities)
                            ingredientsSynced = entities.size
                        }
                    }.onFailure { anyNetworkError = it }

                    val favoriteIds = recipeDao.getFavoriteIds().toSet()
                    val remoteMeals = mealsDeferred.awaitAll().flatMap { res ->
                        res.onFailure { if (anyNetworkError == null) anyNetworkError = it }
                        res.getOrNull()?.meals.orEmpty()
                    }

                    val recipeEntities = remoteMeals
                        .distinctBy { it.idMeal }
                        .mapNotNull { dto ->
                            val id = dto.idMeal?.trim().orEmpty()
                            recipeMapper.dtoToEntity(
                                dto = dto,
                                isFavorite = id in favoriteIds,
                                now = now
                            )
                        }

                    if (recipeEntities.isNotEmpty()) {
                        recipeDao.upsertRecipes(recipeEntities)
                        recipesSynced = recipeEntities.size
                    }
                }

                if (force && recipesSynced == 0 && ingredientsSynced == 0 && anyNetworkError != null) {
                    Result.failure(anyNetworkError!!)
                } else {
                    Result.success(
                        SyncSummary(
                            recipesSynced = recipesSynced,
                            ingredientsSynced = ingredientsSynced,
                            fromCacheOnly = recipesSynced == 0 && ingredientsSynced == 0
                        )
                    )
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }

    override suspend fun searchRecipes(query: String): Result<List<Recipe>> =
        withContext(ioDispatcher) {
            try {
                ensureSeeded()
                val trimmed = query.trim()
                if (trimmed.isEmpty()) {
                    val all = recipeDao.getAllDetailedRecipes().map(recipeMapper::entityToDomain)
                    return@withContext Result.success(all)
                }

                if (connectivityObserver.currentIsOnline()) {
                    runCatchingNonCancellation { api.searchMealsByName(trimmed) }
                        .onSuccess { response ->
                            val favoriteIds = recipeDao.getFavoriteIds().toSet()
                            val now = System.currentTimeMillis()
                            val entities = response.meals.orEmpty().mapNotNull { dto ->
                                val id = dto.idMeal?.trim().orEmpty()
                                recipeMapper.dtoToEntity(dto, isFavorite = id in favoriteIds, now = now)
                            }
                            if (entities.isNotEmpty()) {
                                recipeDao.upsertRecipes(entities)
                            }
                        }
                }

                val localResults = recipeDao.searchRecipesLocal(trimmed).map(recipeMapper::entityToDomain)
                Result.success(localResults)
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }

    override suspend fun getRecipeDetail(id: String, forceRefresh: Boolean): Result<Recipe> =
        withContext(ioDispatcher) {
            try {
                ensureSeeded()
                val local = recipeDao.getRecipeById(id)
                if (local != null && local.hasFullDetails && !forceRefresh) {
                    return@withContext Result.success(recipeMapper.entityToDomain(local))
                }

                if (connectivityObserver.currentIsOnline()) {
                    val remoteResult = runCatchingNonCancellation { api.lookupMealById(id) }
                    val remoteDto = remoteResult.getOrNull()?.meals?.firstOrNull()
                    if (remoteDto != null) {
                        val entity = recipeMapper.dtoToEntity(
                            dto = remoteDto,
                            isFavorite = local?.isFavorite == true
                        )
                        if (entity != null) {
                            recipeDao.upsertRecipe(entity)
                            return@withContext Result.success(recipeMapper.entityToDomain(entity))
                        }
                    } else if (local == null && remoteResult.isFailure) {
                        return@withContext Result.failure(
                            remoteResult.exceptionOrNull()
                                ?: IllegalStateException("Não foi possível carregar os detalhes da receita.")
                        )
                    }
                }

                if (local != null) {
                    Result.success(recipeMapper.entityToDomain(local))
                } else {
                    Result.failure(IllegalStateException("Receita indisponível no modo offline."))
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }

    override suspend fun getCandidateRecipesForIngredients(ingredients: List<String>): Result<List<Recipe>> =
        withContext(ioDispatcher) {
            try {
                ensureSeeded()

                if (ingredients.isNotEmpty() && connectivityObserver.currentIsOnline()) {
                    runCatchingNonCancellation {
                        val queryItems = ingredients.take(MAX_REMOTE_FILTER_ITEMS)
                        val filterResponses = coroutineScope {
                            queryItems.map { item ->
                                async {
                                    runCatchingNonCancellation {
                                        api.filterMealsByIngredient(item.trim())
                                    }
                                }
                            }.awaitAll()
                        }

                        val frequency = linkedMapOf<String, Int>()
                        for (response in filterResponses) {
                            response.getOrNull()?.meals.orEmpty().forEach { filtered ->
                                val id = filtered.idMeal?.trim().orEmpty()
                                if (id.isNotEmpty()) {
                                    frequency[id] = (frequency[id] ?: 0) + 1
                                }
                            }
                        }

                        val candidateIdsToHydrate = frequency.entries
                            .sortedByDescending { it.value }
                            .map { it.key }
                            .filter { id ->
                                val existing = recipeDao.getRecipeById(id)
                                existing == null || !existing.hasFullDetails
                            }
                            .take(MAX_HYDRATE_DETAILS_PER_QUERY)

                        if (candidateIdsToHydrate.isNotEmpty()) {
                            val favoriteIds = recipeDao.getFavoriteIds().toSet()
                            val now = System.currentTimeMillis()
                            val detailedEntities = coroutineScope {
                                candidateIdsToHydrate.map { mealId ->
                                    async {
                                        runCatchingNonCancellation { api.lookupMealById(mealId) }
                                            .getOrNull()
                                            ?.meals
                                            ?.firstOrNull()
                                            ?.let { dto ->
                                                recipeMapper.dtoToEntity(
                                                    dto = dto,
                                                    isFavorite = mealId in favoriteIds,
                                                    now = now
                                                )
                                            }
                                    }
                                }.awaitAll().filterNotNull()
                            }

                            if (detailedEntities.isNotEmpty()) {
                                recipeDao.upsertRecipes(detailedEntities)
                            }
                        }
                    }
                }

                val detailedLocal = recipeDao.getAllDetailedRecipes().map(recipeMapper::entityToDomain)
                Result.success(detailedLocal)
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }

    override suspend fun toggleFavorite(recipeId: String): Result<Boolean> =
        withContext(ioDispatcher) {
            try {
                val existing = recipeDao.getRecipeById(recipeId)
                    ?: return@withContext Result.failure(
                        NoSuchElementException("Receita não encontrada no banco local.")
                    )
                val nextValue = !existing.isFavorite
                recipeDao.updateFavorite(recipeId, nextValue)
                Result.success(nextValue)
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }

    private suspend fun ensureSeeded() {
        if (recipeDao.countRecipes() == 0) {
            recipeDao.upsertRecipes(LocalSeedData.seedRecipes(gson))
        }
        if (ingredientDao.countIngredients() == 0) {
            ingredientDao.upsertAll(LocalSeedData.seedIngredients())
        }
    }

    private inline fun <T> runCatchingNonCancellation(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    private companion object {
        val SYNC_TTL_MILLIS = TimeUnit.HOURS.toMillis(12)
        const val MIN_ONLINE_RECIPE_COUNT = 12
        const val MAX_REMOTE_FILTER_ITEMS = 4
        const val MAX_HYDRATE_DETAILS_PER_QUERY = 10
    }
}
