package br.edu.orlandovandres.temperocerto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.edu.orlandovandres.temperocerto.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes ORDER BY isFavorite DESC, name ASC")
    fun observeAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY name ASC")
    fun observeFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    fun observeRecipeById(id: String): Flow<RecipeEntity?>

    @Query(
        """
        SELECT * FROM recipes
        WHERE name LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
           OR area LIKE '%' || :query || '%'
           OR ingredientsJson LIKE '%' || :query || '%'
        ORDER BY isFavorite DESC, name ASC
        """
    )
    suspend fun searchRecipesLocal(query: String): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE hasFullDetails = 1 ORDER BY isFavorite DESC, name ASC")
    suspend fun getAllDetailedRecipes(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getRecipeById(id: String): RecipeEntity?

    @Query("SELECT id FROM recipes WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipes(entities: List<RecipeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipe(entity: RecipeEntity)

    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun countRecipes(): Int
}
