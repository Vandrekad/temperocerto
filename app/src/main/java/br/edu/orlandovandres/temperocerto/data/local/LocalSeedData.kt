package br.edu.orlandovandres.temperocerto.data.local

import br.edu.orlandovandres.temperocerto.data.local.entity.IngredientCatalogEntity
import br.edu.orlandovandres.temperocerto.data.local.entity.RecipeEntity
import br.edu.orlandovandres.temperocerto.domain.model.IngredientKind
import br.edu.orlandovandres.temperocerto.domain.model.IngredientMeasure
import br.edu.orlandovandres.temperocerto.domain.model.SpiceClassifier
import com.google.gson.Gson

/**
 * Conjunto inicial curado para garantir funcionamento Offline First completo
 * mesmo na primeira abertura do aplicativo sem conexão com a internet.
 */
object LocalSeedData {

    fun seedIngredients(now: Long = System.currentTimeMillis()): List<IngredientCatalogEntity> {
        val items = listOf(
            Triple("seed-ing-1", "Garlic", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-2", "Ginger", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-3", "Cumin", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-4", "Paprika", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-5", "Oregano", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-6", "Thyme", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-7", "Rosemary", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-8", "Turmeric", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-9", "Coriander", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-10", "Parsley", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-11", "Basil", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-12", "Black Pepper", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-13", "Chili Powder", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-14", "Cinnamon", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-15", "Bay Leaf", IngredientKind.SPICE_OR_SEASONING),
            Triple("seed-ing-16", "Soy Sauce", IngredientKind.SAUCE_OR_CONDIMENT),
            Triple("seed-ing-17", "Olive Oil", IngredientKind.SAUCE_OR_CONDIMENT),
            Triple("seed-ing-18", "Lemon", IngredientKind.SAUCE_OR_CONDIMENT),
            Triple("seed-ing-19", "Mustard", IngredientKind.SAUCE_OR_CONDIMENT),
            Triple("seed-ing-20", "Honey", IngredientKind.SAUCE_OR_CONDIMENT),
            Triple("seed-ing-21", "Chicken", IngredientKind.PROTEIN),
            Triple("seed-ing-22", "Beef", IngredientKind.PROTEIN),
            Triple("seed-ing-23", "Salmon", IngredientKind.PROTEIN),
            Triple("seed-ing-24", "Shrimp", IngredientKind.PROTEIN),
            Triple("seed-ing-25", "Eggs", IngredientKind.PROTEIN),
            Triple("seed-ing-26", "Onion", IngredientKind.VEGETABLE),
            Triple("seed-ing-27", "Tomato", IngredientKind.VEGETABLE),
            Triple("seed-ing-28", "Potatoes", IngredientKind.VEGETABLE),
            Triple("seed-ing-29", "Rice", IngredientKind.GRAIN_OR_PASTA),
            Triple("seed-ing-30", "Butter", IngredientKind.DAIRY)
        )

        return items.map { (id, name, kind) ->
            IngredientCatalogEntity(
                id = id,
                name = name,
                description = "Ingrediente essencial do catálogo offline TemperoCerto",
                kind = kind.name,
                thumbnailUrl = "https://www.themealdb.com/images/ingredients/${name.lowercase().replace(" ", "%20")}.png",
                cachedAt = now
            )
        }
    }

    fun seedRecipes(gson: Gson, now: Long = System.currentTimeMillis()): List<RecipeEntity> {
        fun jsonOf(vararg pairs: Pair<String, String>): String {
            val list = pairs.map { (name, measure) ->
                IngredientMeasure(
                    name = name,
                    measure = measure,
                    kind = SpiceClassifier.classify(name)
                )
            }
            return gson.toJson(list)
        }

        return listOf(
            RecipeEntity(
                id = "52772",
                name = "Teriyaki Chicken Casserole",
                category = "Chicken",
                area = "Japanese",
                instructions = "Combine soy sauce, water, brown sugar, ginger and minced garlic in a saucepan and simmer until thickened. Pour over chicken breasts, bake 35 minutes, shred and toss with steamed vegetables and rice.",
                thumbnailUrl = "https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg",
                tagsCsv = "Meat,Casserole",
                youtubeUrl = "https://www.youtube.com/watch?v=4aZr5hZXP_s",
                ingredientsJson = jsonOf(
                    "soy sauce" to "3/4 cup",
                    "ground ginger" to "1/2 teaspoon",
                    "minced garlic" to "1/2 teaspoon",
                    "chicken breasts" to "2",
                    "brown rice" to "3 cups",
                    "stir-fry vegetables" to "12 oz"
                ),
                isFavorite = false,
                hasFullDetails = true,
                cachedAt = now
            ),
            RecipeEntity(
                id = "52959",
                name = "Baked Salmon with Fennel & Tomatoes",
                category = "Seafood",
                area = "British",
                instructions = "Heat oven to 180C. Toss chopped fennel, cherry tomatoes, garlic, olive oil, thyme and black pepper in a roasting tin. Top with salmon fillets, drizzle lemon juice and bake for 20 minutes.",
                thumbnailUrl = "https://www.themealdb.com/images/media/meals/1548772327.jpg",
                tagsCsv = "Seafood,Healthy",
                youtubeUrl = null,
                ingredientsJson = jsonOf(
                    "Salmon" to "2 fillets",
                    "Garlic" to "2 cloves",
                    "Thyme" to "2 sprigs",
                    "Black Pepper" to "1/2 tsp",
                    "Olive Oil" to "2 tbsp",
                    "Lemon" to "1",
                    "Tomato" to "250g"
                ),
                isFavorite = false,
                hasFullDetails = true,
                cachedAt = now
            ),
            RecipeEntity(
                id = "52819",
                name = "Cajun Spiced Fish Tacos",
                category = "Seafood",
                area = "Mexican",
                instructions = "Coat fish fillets with paprika, cumin, garlic powder, oregano and cayenne pepper. Sear in olive oil until flaky and serve in warm tortillas with cilantro and lime.",
                thumbnailUrl = "https://www.themealdb.com/images/media/meals/uvuyxu1503067369.jpg",
                tagsCsv = "Spicy,Fish",
                youtubeUrl = null,
                ingredientsJson = jsonOf(
                    "Paprika" to "1 tbsp",
                    "Cumin" to "1 tsp",
                    "Garlic" to "2 cloves",
                    "Oregano" to "1 tsp",
                    "Coriander" to "1/2 bunch",
                    "Lime" to "1",
                    "Olive Oil" to "2 tbsp",
                    "Fish" to "400g"
                ),
                isFavorite = false,
                hasFullDetails = true,
                cachedAt = now
            ),
            RecipeEntity(
                id = "52807",
                name = "Baingan Bharta (Roasted Spiced Eggplant)",
                category = "Vegetarian",
                area = "Indian",
                instructions = "Roast eggplant until charred. Saute onion, garlic, ginger, cumin, turmeric, coriander and chili powder in oil. Add chopped tomato and mashed eggplant and simmer for 12 minutes.",
                thumbnailUrl = "https://www.themealdb.com/images/media/meals/urtpqw1487341253.jpg",
                tagsCsv = "Vegetarian,Spicy",
                youtubeUrl = null,
                ingredientsJson = jsonOf(
                    "Eggplant" to "2 large",
                    "Garlic" to "4 cloves",
                    "Ginger" to "1 inch",
                    "Cumin" to "1 tsp",
                    "Turmeric" to "1/2 tsp",
                    "Coriander" to "1 tsp",
                    "Chili Powder" to "1/2 tsp",
                    "Onion" to "1 large",
                    "Tomato" to "2 medium"
                ),
                isFavorite = false,
                hasFullDetails = true,
                cachedAt = now
            ),
            RecipeEntity(
                id = "52795",
                name = "Chicken Handi",
                category = "Chicken",
                area = "Indian",
                instructions = "Brown onions with garlic and ginger paste. Add chicken, cumin, turmeric, coriander, paprika and garam masala. Stir in yogurt and tomatoes and simmer gently until tender.",
                thumbnailUrl = "https://www.themealdb.com/images/media/meals/wyxwsp1486979827.jpg",
                tagsCsv = "Curry,Chicken",
                youtubeUrl = null,
                ingredientsJson = jsonOf(
                    "Chicken" to "750g",
                    "Garlic" to "5 cloves",
                    "Ginger" to "1 tbsp",
                    "Cumin" to "1 tsp",
                    "Turmeric" to "1 tsp",
                    "Coriander" to "1 tbsp",
                    "Paprika" to "1 tsp",
                    "Onion" to "2",
                    "Tomato" to "3"
                ),
                isFavorite = false,
                hasFullDetails = true,
                cachedAt = now
            ),
            RecipeEntity(
                id = "52874",
                name = "Beef and Mustard Pie",
                category = "Beef",
                area = "British",
                instructions = "Sear beef cubes in butter, then braise slowly with garlic, thyme, rosemary, bay leaf, mustard and beef stock until tender. Top with pastry and bake until golden.",
                thumbnailUrl = "https://www.themealdb.com/images/media/meals/sytuqu1511553755.jpg",
                tagsCsv = "Beef,Pie",
                youtubeUrl = null,
                ingredientsJson = jsonOf(
                    "Beef" to "800g",
                    "Mustard" to "2 tbsp",
                    "Garlic" to "2 cloves",
                    "Thyme" to "3 sprigs",
                    "Rosemary" to "1 sprig",
                    "Bay Leaf" to "2",
                    "Black Pepper" to "1 tsp",
                    "Onion" to "1",
                    "Butter" to "30g"
                ),
                isFavorite = false,
                hasFullDetails = true,
                cachedAt = now
            ),
            RecipeEntity(
                id = "52777",
                name = "Mediterranean Pasta Salad",
                category = "Pasta",
                area = "Italian",
                instructions = "Cook pasta al dente and cool. Toss with olive oil, minced garlic, oregano, basil, parsley, lemon juice, cherry tomatoes and black pepper.",
                thumbnailUrl = "https://www.themealdb.com/images/media/meals/wvqpwt1468339226.jpg",
                tagsCsv = "Pasta,Mediterranean",
                youtubeUrl = null,
                ingredientsJson = jsonOf(
                    "Pasta" to "300g",
                    "Olive Oil" to "4 tbsp",
                    "Garlic" to "2 cloves",
                    "Oregano" to "1 tsp",
                    "Basil" to "1/2 cup",
                    "Parsley" to "1/4 cup",
                    "Lemon" to "1",
                    "Tomato" to "200g",
                    "Black Pepper" to "1/2 tsp"
                ),
                isFavorite = false,
                hasFullDetails = true,
                cachedAt = now
            ),
            RecipeEntity(
                id = "52934",
                name = "Chicken & Rosemary Roasted Potatoes",
                category = "Chicken",
                area = "Portuguese",
                instructions = "Marinate chicken thighs with crushed garlic, smoked paprika, rosemary, thyme, lemon juice and olive oil. Roast alongside cubed potatoes until crisp and golden.",
                thumbnailUrl = "https://www.themealdb.com/images/media/meals/020z181619788503.jpg",
                tagsCsv = "Roast,Herbs",
                youtubeUrl = null,
                ingredientsJson = jsonOf(
                    "Chicken" to "600g",
                    "Potatoes" to "500g",
                    "Garlic" to "4 cloves",
                    "Paprika" to "1 tbsp",
                    "Rosemary" to "2 sprigs",
                    "Thyme" to "2 sprigs",
                    "Olive Oil" to "3 tbsp",
                    "Lemon" to "1"
                ),
                isFavorite = false,
                hasFullDetails = true,
                cachedAt = now
            )
        )
    }
}
