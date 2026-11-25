package br.edu.orlandovandres.temperocerto.presentation.recipes

import androidx.compose.runtime.Immutable
import br.edu.orlandovandres.temperocerto.domain.model.Recipe

@Immutable
data class RecipesUiState(
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val isOnline: Boolean = true,
    val searchQuery: String = "",
    val selectedCategory: String = ALL_CATEGORIES,
    val categories: List<String> = listOf(ALL_CATEGORIES),
    val recipes: List<Recipe> = emptyList(),
    val errorMessage: String? = null
) {
    companion object {
        const val ALL_CATEGORIES = "Todas"
    }
}
