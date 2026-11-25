package br.edu.orlandovandres.temperocerto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import br.edu.orlandovandres.temperocerto.data.local.dao.IngredientCatalogDao
import br.edu.orlandovandres.temperocerto.data.local.dao.RecipeDao
import br.edu.orlandovandres.temperocerto.data.local.entity.IngredientCatalogEntity
import br.edu.orlandovandres.temperocerto.data.local.entity.RecipeEntity

@Database(
    entities = [
        RecipeEntity::class,
        IngredientCatalogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TemperoDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientCatalogDao(): IngredientCatalogDao
}
