package br.edu.orlandovandres.temperocerto.presentation.combiner

import app.cash.turbine.test
import br.edu.orlandovandres.temperocerto.domain.model.SyncSummary
import br.edu.orlandovandres.temperocerto.domain.repository.ConnectivityObserver
import br.edu.orlandovandres.temperocerto.domain.repository.RecipeRepository
import br.edu.orlandovandres.temperocerto.domain.usecase.FindCombinationsUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.ObserveIngredientCatalogUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.ObserveRecipesUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.SuggestSpicePairingsUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.SyncCatalogUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.ToggleFavoriteRecipeUseCase
import br.edu.orlandovandres.temperocerto.util.MainDispatcherRule
import br.edu.orlandovandres.temperocerto.util.TestFixtures
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CombinerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: RecipeRepository
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var viewModel: CombinerViewModel

    private val recipesFlow = MutableStateFlow(
        listOf(
            TestFixtures.sampleRecipe(
                id = "1",
                name = "Garlic Ginger Chicken",
                ingredients = listOf(
                    "Chicken" to "500g",
                    "Garlic" to "3 cloves",
                    "Ginger" to "1 tsp",
                    "Cumin" to "1 tsp"
                )
            )
        )
    )
    private val catalogFlow = MutableStateFlow(TestFixtures.sampleCatalog())
    private val onlineFlow = MutableStateFlow(true)

    @Before
    fun setUp() {
        repository = mockk()
        connectivityObserver = mockk()

        every { repository.observeRecipes() } returns recipesFlow
        every { repository.observeIngredientCatalog() } returns catalogFlow
        every { connectivityObserver.isOnline } returns onlineFlow
        coEvery { repository.syncCatalog(any()) } returns Result.success(SyncSummary(1, 5, false))
        coEvery { repository.getCandidateRecipesForIngredients(any()) } returns Result.success(recipesFlow.value)

        viewModel = CombinerViewModel(
            observeIngredientCatalogUseCase = ObserveIngredientCatalogUseCase(repository),
            observeRecipesUseCase = ObserveRecipesUseCase(repository),
            findCombinationsUseCase = FindCombinationsUseCase(repository),
            suggestSpicePairingsUseCase = SuggestSpicePairingsUseCase(),
            toggleFavoriteRecipeUseCase = ToggleFavoriteRecipeUseCase(repository),
            syncCatalogUseCase = SyncCatalogUseCase(repository),
            connectivityObserver = connectivityObserver
        )
    }

    @Test
    fun `adding and removing items updates selectedItems, matches and suggested pairings`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertFalse(initial.isLoadingCatalog)
            assertTrue(initial.selectedItems.isEmpty())
            assertTrue(initial.suggestedPairings.isNotEmpty())

            viewModel.onAddItem("Garlic")
            val afterAdd = expectMostRecentItem()
            assertEquals(listOf("Garlic"), afterAdd.selectedItems)
            assertEquals(1, afterAdd.matches.size)
            assertEquals("Garlic Ginger Chicken", afterAdd.matches.first().recipe.name)
            assertTrue(afterAdd.suggestedPairings.any { it.spiceName == "Ginger" })

            viewModel.onRemoveItem("Garlic")
            val afterRemove = expectMostRecentItem()
            assertTrue(afterRemove.selectedItems.isEmpty())
            assertTrue(afterRemove.matches.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reflects offline connectivity state banner accurately`() = runTest {
        viewModel.uiState.test {
            val onlineState = awaitItem()
            assertTrue(onlineState.isOnline)

            onlineFlow.value = false
            val offlineState = awaitItem()
            assertFalse(offlineState.isOnline)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
