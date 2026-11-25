package br.edu.orlandovandres.temperocerto.presentation.recipes

import app.cash.turbine.test
import br.edu.orlandovandres.temperocerto.domain.model.SyncSummary
import br.edu.orlandovandres.temperocerto.domain.repository.ConnectivityObserver
import br.edu.orlandovandres.temperocerto.domain.repository.RecipeRepository
import br.edu.orlandovandres.temperocerto.domain.usecase.ObserveRecipesUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.SearchRecipesUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.SyncCatalogUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.ToggleFavoriteRecipeUseCase
import br.edu.orlandovandres.temperocerto.util.MainDispatcherRule
import br.edu.orlandovandres.temperocerto.util.TestFixtures
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RecipesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: RecipeRepository
    private lateinit var connectivityObserver: ConnectivityObserver

    private val recipesFlow = MutableStateFlow(
        listOf(
            TestFixtures.sampleRecipe("1", "Chicken Handi", category = "Chicken"),
            TestFixtures.sampleRecipe("2", "Salmon Fennel", category = "Seafood")
        )
    )
    private val onlineFlow = MutableStateFlow(true)

    @Before
    fun setUp() {
        repository = mockk()
        connectivityObserver = mockk()
        every { repository.observeRecipes() } returns recipesFlow
        every { connectivityObserver.isOnline } returns onlineFlow
        coEvery { repository.searchRecipes(any()) } returns Result.success(recipesFlow.value)
        coEvery { repository.toggleFavorite(any()) } returns Result.success(true)
    }

    @Test
    fun `filters recipes by category and query via StateFlow`() = runTest {
        coEvery { repository.syncCatalog(any()) } returns Result.success(SyncSummary(2, 5, false))

        val viewModel = RecipesViewModel(
            observeRecipesUseCase = ObserveRecipesUseCase(repository),
            searchRecipesUseCase = SearchRecipesUseCase(repository),
            toggleFavoriteRecipeUseCase = ToggleFavoriteRecipeUseCase(repository),
            syncCatalogUseCase = SyncCatalogUseCase(repository),
            connectivityObserver = connectivityObserver
        )

        viewModel.uiState.test {
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(2, loaded.recipes.size)

            viewModel.onCategorySelected("Seafood")
            val seafoodOnly = awaitItem()
            assertEquals(1, seafoodOnly.recipes.size)
            assertEquals("Salmon Fennel", seafoodOnly.recipes.first().name)

            viewModel.onCategorySelected(RecipesUiState.ALL_CATEGORIES)
            awaitItem()
            viewModel.onSearchQueryChange("Handi")
            val handiOnly = awaitItem()
            assertEquals(1, handiOnly.recipes.size)
            assertEquals("Chicken Handi", handiOnly.recipes.first().name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `exposes error message when forced sync fails while keeping cached recipes`() = runTest {
        coEvery { repository.syncCatalog(false) } returns Result.success(SyncSummary(2, 5, false))
        coEvery { repository.syncCatalog(true) } returns Result.failure(IOException("Sem rede"))

        val viewModel = RecipesViewModel(
            observeRecipesUseCase = ObserveRecipesUseCase(repository),
            searchRecipesUseCase = SearchRecipesUseCase(repository),
            toggleFavoriteRecipeUseCase = ToggleFavoriteRecipeUseCase(repository),
            syncCatalogUseCase = SyncCatalogUseCase(repository),
            connectivityObserver = connectivityObserver
        )

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(2, initial.recipes.size)

            viewModel.onRefresh()
            val errorState = expectMostRecentItem()
            assertEquals("Sem rede", errorState.errorMessage)
            assertEquals(2, errorState.recipes.size)
            assertTrue(errorState.errorMessage != null)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
