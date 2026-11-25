package br.edu.orlandovandres.temperocerto.presentation.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Recipes : Screen("recipes")
    data object Combiner : Screen("combiner")
    data object Favorites : Screen("favorites")
    data object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: String): String =
            "recipe_detail/${Uri.encode(recipeId)}"
    }
}
