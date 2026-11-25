package br.edu.orlandovandres.temperocerto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.edu.orlandovandres.temperocerto.data.local.entity.IngredientCatalogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientCatalogDao {

    @Query("SELECT * FROM ingredient_catalog ORDER BY name ASC")
    fun observeAllIngredients(): Flow<List<IngredientCatalogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<IngredientCatalogEntity>)

    @Query("SELECT COUNT(*) FROM ingredient_catalog")
    suspend fun countIngredients(): Int

    @Query("SELECT MAX(cachedAt) FROM ingredient_catalog")
    suspend fun getLatestCachedAt(): Long?
}
