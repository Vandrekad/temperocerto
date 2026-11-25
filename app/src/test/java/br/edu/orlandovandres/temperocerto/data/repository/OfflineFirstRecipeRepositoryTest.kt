package br.edu.orlandovandres.temperocerto.data.repository

import br.edu.orlandovandres.temperocerto.data.local.dao.IngredientCatalogDao
import br.edu.orlandovandres.temperocerto.data.local.dao.RecipeDao
import br.edu.orlandovandres.temperocerto.data.local.entity.RecipeEntity
import br.edu.orlandovandres.temperocerto.data.mapper.IngredientMapper
import br.edu.orlandovandres.temperocerto.data.mapper.RecipeMapper
import br.edu.orlandovandres.temperocerto.data.remote.api.TheMealDbApi
import br.edu.orlandovandres.temperocerto.data.remote.dto.IngredientDto
import br.edu.orlandovandres.temperocerto.data.remote.dto.IngredientListResponseDto
import br.edu.orlandovandres.temperocerto.data.remote.dto.MealDto
import br.edu.orlandovandres.temperocerto.data.remote.dto.MealListResponseDto
import br.edu.orlandovandres.temperocerto.domain.repository.ConnectivityObserver
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.io.IOException
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class OfflineFirstRecipeRepositoryTest {

    private lateinit var api: TheMealDbApi
    private lateinit var recipeDao: RecipeDao
    private lateinit var ingredientDao: IngredientCatalogDao
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var gson: Gson
    private lateinit var repository: OfflineFirstRecipeRepository

    @Before
    fun setUp() {
        api = mockk()
        recipeDao = mockk(relaxed = true)
        ingredientDao = mockk(relaxed = true)
        connectivityObserver = mockk()
        gson = Gson()

        repository = OfflineFirstRecipeRepository(
            api = api,
            recipeDao = recipeDao,
            ingredientDao = ingredientDao,
            recipeMapper = RecipeMapper(gson),
            ingredientMapper = IngredientMapper(),
            connectivityObserver = connectivityObserver,
            gson = gson,
            ioDispatcher = UnconfinedTestDispatcher()
        )
    }

    @Test
    fun `syncCatalog preserves local favorite flag when upserting remote recipes`() = runTest {
        every { connectivityObserver.currentIsOnline() } returns true
        coEvery { recipeDao.countRecipes() } returns 10
        coEvery { ingredientDao.countIngredients() } returns 10
        coEvery { ingredientDao.getLatestCachedAt() } returns null
        coEvery { recipeDao.getFavoriteIds() } returns listOf("52772")

        coEvery { api.listIngredients() } returns IngredientListResponseDto(
            listOf(IngredientDto("1", "Garlic", null, "Seasoning", null))
        )
        coEvery { api.searchMealsByFirstLetter(any()) } returns MealListResponseDto(
            listOf(
                MealDto(
                    idMeal = "52772",
                    strMeal = "Teriyaki Chicken",
                    strCategory = "Chicken",
                    strArea = "Japanese",
                    strInstructions = "Cook",
                    strMealThumb = null,
                    strTags = null,
                    strYoutube = null,
                    strIngredient1 = "Garlic",
                    strMeasure1 = "2 cloves"
                ),
                MealDto(
                    idMeal = "52999",
                    strMeal = "Spicy Beef",
                    strCategory = "Beef",
                    strArea = "Mexican",
                    strInstructions = "Grill",
                    strMealThumb = null,
                    strTags = null,
                    strYoutube = null,
                    strIngredient1 = "Cumin",
                    strMeasure1 = "1 tsp"
                )
            )
        )

        val upsertedSlot = slot<List<RecipeEntity>>()
        coEvery { recipeDao.upsertRecipes(capture(upsertedSlot)) } returns Unit

        val summary = repository.syncCatalog(force = true).getOrThrow()

        assertFalse(summary.fromCacheOnly)
        assertEquals(2, upsertedSlot.captured.size)
        val favEntity = upsertedSlot.captured.first { it.id == "52772" }
        val nonFavEntity = upsertedSlot.captured.first { it.id == "52999" }
        assertTrue(favEntity.isFavorite)
        assertFalse(nonFavEntity.isFavorite)
    }

    @Test
    fun `searchRecipes falls back to local Room data without crash when remote fails`() = runTest {
        every { connectivityObserver.currentIsOnline() } returns true
        coEvery { recipeDao.countRecipes() } returns 8
        coEvery { ingredientDao.countIngredients() } returns 30
        coEvery { api.searchMealsByName("garlic") } throws IOException("Timeout")
        coEvery { recipeDao.searchRecipesLocal("garlic") } returns listOf(
            RecipeEntity(
                id = "1",
                name = "Garlic Chicken",
                category = "Chicken",
                area = "Brazilian",
                instructions = "Roast",
                thumbnailUrl = null,
                tagsCsv = "",
                youtubeUrl = null,
                ingredientsJson = "[]",
                isFavorite = false,
                hasFullDetails = true,
                cachedAt = 1L
            )
        )

        val result = repository.searchRecipes("garlic")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("Garlic Chicken", result.getOrThrow().first().name)
    }

    @Test
    fun `getCandidateRecipesForIngredients returns local recipes immediately when offline`() = runTest {
        every { connectivityObserver.currentIsOnline() } returns false
        coEvery { recipeDao.countRecipes() } returns 8
        coEvery { ingredientDao.countIngredients() } returns 30
        coEvery { recipeDao.getAllDetailedRecipes() } returns listOf(
            RecipeEntity(
                id = "52772",
                name = "Teriyaki Chicken",
                category = "Chicken",
                area = "Japanese",
                instructions = "Cook",
                thumbnailUrl = null,
                tagsCsv = "",
                youtubeUrl = null,
                ingredientsJson = "[]",
                isFavorite = true,
                hasFullDetails = true,
                cachedAt = 1L
            )
        )

        val result = repository.getCandidateRecipesForIngredients(listOf("Garlic", "Ginger"))

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        coVerify(exactly = 0) { api.filterMealsByIngredient(any()) }
    }

    @Test
    fun `toggleFavorite flips boolean flag in DAO`() = runTest {
        coEvery { recipeDao.getRecipeById("52772") } returns RecipeEntity(
            id = "52772",
            name = "Teriyaki",
            category = "Chicken",
            area = "Japanese",
            instructions = "Cook",
            thumbnailUrl = null,
            tagsCsv = "",
            youtubeUrl = null,
            ingredientsJson = "[]",
            isFavorite = false,
            hasFullDetails = true,
            cachedAt = 1L
        )

        val result = repository.toggleFavorite("52772")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
        coVerify(exactly = 1) { recipeDao.updateFavorite("52772", true) }
    }
}
