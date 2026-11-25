package br.edu.orlandovandres.temperocerto.domain.usecase

import br.edu.orlandovandres.temperocerto.domain.model.IngredientKind
import br.edu.orlandovandres.temperocerto.domain.model.SpiceClassifier
import br.edu.orlandovandres.temperocerto.domain.repository.RecipeRepository
import br.edu.orlandovandres.temperocerto.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeUseCasesTest {

    private val repository: RecipeRepository = mockk()

    @Test
    fun `SearchRecipesUseCase trims query before calling repository`() = runTest {
        val searchUseCase = SearchRecipesUseCase(repository)
        val expected = listOf(TestFixtures.sampleRecipe("1", "Chicken Curry"))
        coEvery { repository.searchRecipes("curry") } returns Result.success(expected)

        val result = searchUseCase("  curry  ")

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrThrow())
        coVerify(exactly = 1) { repository.searchRecipes("curry") }
    }

    @Test
    fun `ToggleFavoriteRecipeUseCase delegates to repository`() = runTest {
        val toggleUseCase = ToggleFavoriteRecipeUseCase(repository)
        coEvery { repository.toggleFavorite("52772") } returns Result.success(true)

        val result = toggleUseCase("52772")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    @Test
    fun `SpiceClassifier categorizes spices, sauces, proteins and vegetables accurately`() {
        assertEquals(IngredientKind.SPICE_OR_SEASONING, SpiceClassifier.classify("Smoked Paprika"))
        assertEquals(IngredientKind.SPICE_OR_SEASONING, SpiceClassifier.classify("Cumin", "Spice"))
        assertEquals(IngredientKind.SAUCE_OR_CONDIMENT, SpiceClassifier.classify("Soy Sauce"))
        assertEquals(IngredientKind.PROTEIN, SpiceClassifier.classify("Chicken Breast"))
        assertEquals(IngredientKind.VEGETABLE, SpiceClassifier.classify("Tomato"))
        assertTrue(SpiceClassifier.isSuggestableSpice("Ginger"))
        assertFalse(SpiceClassifier.isSuggestableSpice("Boiling Water"))
        assertFalse(SpiceClassifier.isSuggestableSpice("Salt"))
    }
}
