package br.edu.orlandovandres.temperocerto.domain.usecase

import br.edu.orlandovandres.temperocerto.domain.repository.RecipeRepository
import br.edu.orlandovandres.temperocerto.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FindCombinationsUseCaseTest {

    private lateinit var repository: RecipeRepository
    private lateinit var useCase: FindCombinationsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = FindCombinationsUseCase(repository)
    }

    @Test
    fun `returns empty list when selected items are empty or blank`() = runTest {
        val result = useCase(listOf("   ", ""))

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `ranks full matches before partial matches and prioritizes favorites`() = runTest {
        val fullMatchRecipe = TestFixtures.sampleRecipe(
            id = "1",
            name = "Ginger Garlic Chicken",
            isFavorite = false,
            ingredients = listOf(
                "Chicken" to "500g",
                "minced garlic" to "2 tsp",
                "ground ginger" to "1 tsp",
                "Cumin" to "1 tsp"
            )
        )
        val partialFavRecipe = TestFixtures.sampleRecipe(
            id = "2",
            name = "Garlic Herb Salmon",
            isFavorite = true,
            ingredients = listOf(
                "Salmon" to "400g",
                "Garlic" to "2 cloves",
                "Thyme" to "2 sprigs"
            )
        )
        val partialNonFavRecipe = TestFixtures.sampleRecipe(
            id = "3",
            name = "Basic Chicken Roast",
            isFavorite = false,
            ingredients = listOf(
                "Chicken" to "600g",
                "Rosemary" to "2 sprigs"
            )
        )

        coEvery { repository.getCandidateRecipesForIngredients(any()) } returns Result.success(
            listOf(partialNonFavRecipe, partialFavRecipe, fullMatchRecipe)
        )

        val result = useCase(listOf("Garlic", "Ginger", "Chicken")).getOrThrow()

        assertEquals(3, result.size)
        assertEquals("1", result[0].recipe.id)
        assertTrue(result[0].isFullMatch)
        assertEquals(1.0, result[0].matchScore, 0.001)
        assertTrue(result[0].extraSpicesInRecipe.contains("Cumin"))

        // Empate de 1/3: favorito deve aparecer antes do não favorito
        assertEquals("2", result[1].recipe.id)
        assertFalse(result[1].isFullMatch)
        assertEquals(1.0 / 3.0, result[1].matchScore, 0.001)
        assertEquals(listOf("Ginger", "Chicken"), result[1].missingSelectedItems)

        assertEquals("3", result[2].recipe.id)
    }

    @Test
    fun `matches case-insensitively and by culinary substring`() = runTest {
        val recipe = TestFixtures.sampleRecipe(
            id = "10",
            name = "Teriyaki Bowl",
            ingredients = listOf(
                "soy sauce" to "3/4 cup",
                "fresh minced garlic" to "1 tbsp"
            )
        )
        coEvery { repository.getCandidateRecipesForIngredients(any()) } returns Result.success(listOf(recipe))

        val matches = useCase(listOf("garlic", "SOY SAUCE")).getOrThrow()

        assertEquals(1, matches.size)
        assertTrue(matches.first().isFullMatch)
        assertEquals(listOf("garlic", "SOY SAUCE"), matches.first().matchedItems)
    }

    @Test
    fun `propagates failure when repository returns error`() = runTest {
        coEvery { repository.getCandidateRecipesForIngredients(any()) } returns Result.failure(
            IOException("Falha de leitura")
        )

        val result = useCase(listOf("Garlic"))

        assertTrue(result.isFailure)
        assertEquals("Falha de leitura", result.exceptionOrNull()?.message)
    }
}
