package br.edu.orlandovandres.temperocerto.data.mapper

import br.edu.orlandovandres.temperocerto.data.remote.dto.MealDto
import br.edu.orlandovandres.temperocerto.domain.model.IngredientKind
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeMapperTest {

    private val mapper = RecipeMapper(Gson())

    @Test
    fun `dtoToEntity extracts valid ingredients, ignores blank slots and preserves favorite on round-trip`() {
        val dto = MealDto(
            idMeal = "52772",
            strMeal = "Teriyaki Chicken Casserole",
            strCategory = "Chicken",
            strArea = "Japanese",
            strInstructions = "Bake and glaze.",
            strMealThumb = "https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg",
            strTags = "Meat, Casserole",
            strYoutube = "https://youtube.com/watch?v=123",
            strIngredient1 = "soy sauce",
            strMeasure1 = "3/4 cup",
            strIngredient2 = "minced garlic",
            strMeasure2 = "1/2 tsp",
            strIngredient3 = "chicken breasts",
            strMeasure3 = "2",
            strIngredient4 = "   ",
            strMeasure4 = "1 tsp",
            strIngredient5 = null,
            strMeasure5 = null
        )

        val entity = mapper.dtoToEntity(dto, isFavorite = true, now = 12345L)
        assertNotNull(entity)

        val domain = mapper.entityToDomain(entity!!)
        assertEquals("52772", domain.id)
        assertEquals("Teriyaki Chicken Casserole", domain.name)
        assertTrue(domain.isFavorite)
        assertEquals(listOf("Meat", "Casserole"), domain.tags)
        assertEquals(3, domain.ingredients.size)
        assertEquals(IngredientKind.SAUCE_OR_CONDIMENT, domain.ingredients[0].kind)
        assertEquals(IngredientKind.SPICE_OR_SEASONING, domain.ingredients[1].kind)
        assertEquals(IngredientKind.PROTEIN, domain.ingredients[2].kind)
        assertEquals(2, domain.spicesAndSeasonings.size)
        assertEquals(1, domain.baseIngredients.size)
    }

    @Test
    fun `dtoToEntity returns null when id or name is missing`() {
        val invalid = MealDto(
            idMeal = "",
            strMeal = "Sem ID",
            strCategory = null,
            strArea = null,
            strInstructions = null,
            strMealThumb = null,
            strTags = null,
            strYoutube = null
        )

        assertNull(mapper.dtoToEntity(invalid))
    }
}
