package br.edu.orlandovandres.temperocerto.domain.repository

import br.edu.orlandovandres.temperocerto.domain.model.CatalogIngredient
import br.edu.orlandovandres.temperocerto.domain.model.Recipe
import br.edu.orlandovandres.temperocerto.domain.model.SyncSummary
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun observeRecipes(): Flow<List<Recipe>>
    fun observeFavoriteRecipes(): Flow<List<Recipe>>
    fun observeRecipeById(id: String): Flow<Recipe?>
    fun observeIngredientCatalog(): Flow<List<CatalogIngredient>>

    suspend fun syncCatalog(force: Boolean = false): Result<SyncSummary>
    suspend fun searchRecipes(query: String): Result<List<Recipe>>
    suspend fun getRecipeDetail(id: String, forceRefresh: Boolean = false): Result<Recipe>
    suspend fun getCandidateRecipesForIngredients(ingredients: List<String>): Result<List<Recipe>>
    suspend fun toggleFavorite(recipeId: String): Result<Boolean>
}
