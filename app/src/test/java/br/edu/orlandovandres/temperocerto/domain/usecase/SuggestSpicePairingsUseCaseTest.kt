package br.edu.orlandovandres.temperocerto.domain.usecase

import br.edu.orlandovandres.temperocerto.util.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestSpicePairingsUseCaseTest {

    private val useCase = SuggestSpicePairingsUseCase()

    @Test
    fun `ranks co-occurring spices descending and excludes already selected or neutral items`() {
        val r1 = TestFixtures.sampleRecipe(
            id = "1",
            name = "Curry 1",
            ingredients = listOf(
                "Chicken" to "500g",
                "Garlic" to "3 cloves",
                "Cumin" to "1 tsp",
                "Turmeric" to "1 tsp",
                "Salt" to "1 tsp",
                "Water" to "1 cup"
            )
        )
        val r2 = TestFixtures.sampleRecipe(
            id = "2",
            name = "Curry 2",
            ingredients = listOf(
                "Chicken" to "500g",
                "Garlic" to "2 cloves",
                "Cumin" to "1 tbsp",
                "Paprika" to "1 tsp"
            )
        )
        val r3 = TestFixtures.sampleRecipe(
            id = "3",
            name = "Roast",
            ingredients = listOf(
                "Chicken" to "500g",
                "Rosemary" to "2 sprigs"
            )
        )

        val suggestions = useCase(
            recipes = listOf(r1, r2, r3),
            selectedItems = listOf("Garlic"),
            limit = 5
        )

        // Cumin aparece em 2 receitas; Turmeric, Paprika e Rosemary em 1; Garlic, Salt e Water devem ser excluídos
        assertEquals("Cumin", suggestions.first().spiceName)
        assertEquals(2, suggestions.first().recipeCount)

        val names = suggestions.map { it.spiceName }
        assertFalse(names.contains("Garlic"))
        assertFalse(names.contains("Salt"))
        assertFalse(names.contains("Water"))
        assertFalse(names.contains("Chicken"))
        assertTrue(names.contains("Turmeric"))
        assertTrue(names.contains("Paprika"))
        assertTrue(names.contains("Rosemary"))
    }
}
