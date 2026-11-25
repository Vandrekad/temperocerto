package br.edu.orlandovandres.temperocerto.util

import br.edu.orlandovandres.temperocerto.domain.model.CatalogIngredient
import br.edu.orlandovandres.temperocerto.domain.model.IngredientKind
import br.edu.orlandovandres.temperocerto.domain.model.IngredientMeasure
import br.edu.orlandovandres.temperocerto.domain.model.Recipe
import br.edu.orlandovandres.temperocerto.domain.model.SpiceClassifier

object TestFixtures {

    fun sampleRecipe(
        id: String,
        name: String,
        category: String = "Chicken",
        area: String = "Brazilian",
        isFavorite: Boolean = false,
        ingredients: List<Pair<String, String>> = listOf(
            "Chicken" to "500g",
            "Garlic" to "3 cloves",
            "Ginger" to "1 tsp",
            "Paprika" to "1 tbsp"
        )
    ): Recipe {
        return Recipe(
            id = id,
            name = name,
            category = category,
            area = area,
            instructions = "Cook thoroughly and season to taste.",
            thumbnailUrl = "https://www.themealdb.com/images/media/meals/sample.jpg",
            tags = listOf(category, "Spice"),
            youtubeUrl = null,
            ingredients = ingredients.map { (item, measure) ->
                IngredientMeasure(
                    name = item,
                    measure = measure,
                    kind = SpiceClassifier.classify(item)
                )
            },
            isFavorite = isFavorite,
            hasFullDetails = true,
            cachedAt = 1_700_000_000_000L
        )
    }

    fun sampleCatalog(): List<CatalogIngredient> = listOf(
        CatalogIngredient("1", "Garlic", null, IngredientKind.SPICE_OR_SEASONING, null),
        CatalogIngredient("2", "Ginger", null, IngredientKind.SPICE_OR_SEASONING, null),
        CatalogIngredient("3", "Paprika", null, IngredientKind.SPICE_OR_SEASONING, null),
        CatalogIngredient("4", "Chicken", null, IngredientKind.PROTEIN, null),
        CatalogIngredient("5", "Soy Sauce", null, IngredientKind.SAUCE_OR_CONDIMENT, null)
    )
}
