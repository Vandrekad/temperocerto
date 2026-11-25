package br.edu.orlandovandres.temperocerto.di

import br.edu.orlandovandres.temperocerto.data.repository.OfflineFirstRecipeRepository
import br.edu.orlandovandres.temperocerto.domain.repository.ConnectivityObserver
import br.edu.orlandovandres.temperocerto.domain.repository.RecipeRepository
import br.edu.orlandovandres.temperocerto.util.NetworkConnectivityObserver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(
        impl: OfflineFirstRecipeRepository
    ): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(
        impl: NetworkConnectivityObserver
    ): ConnectivityObserver
}
