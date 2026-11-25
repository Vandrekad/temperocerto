package br.edu.orlandovandres.temperocerto.di

import android.content.Context
import androidx.room.Room
import br.edu.orlandovandres.temperocerto.data.local.TemperoDatabase
import br.edu.orlandovandres.temperocerto.data.local.dao.IngredientCatalogDao
import br.edu.orlandovandres.temperocerto.data.local.dao.RecipeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTemperoDatabase(
        @ApplicationContext context: Context
    ): TemperoDatabase {
        return Room.databaseBuilder(
            context,
            TemperoDatabase::class.java,
            "tempero_certo.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideRecipeDao(database: TemperoDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideIngredientCatalogDao(database: TemperoDatabase): IngredientCatalogDao =
        database.ingredientCatalogDao()
}
