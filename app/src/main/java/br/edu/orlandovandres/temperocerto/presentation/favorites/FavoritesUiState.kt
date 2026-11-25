package br.edu.orlandovandres.temperocerto.presentation.favorites

import androidx.compose.runtime.Immutable
import br.edu.orlandovandres.temperocerto.domain.model.Recipe

@Immutable
data class FavoritesUiState(
    val isLoading: Boolean = true,
    val isOnline: Boolean = true,
    val searchQuery: String = "",
    val favorites: List<Recipe> = emptyList()
)
