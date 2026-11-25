package br.edu.orlandovandres.temperocerto.presentation.detail

import androidx.compose.runtime.Immutable
import br.edu.orlandovandres.temperocerto.domain.model.Recipe

@Immutable
data class RecipeDetailUiState(
    val isLoading: Boolean = true,
    val isOnline: Boolean = true,
    val recipe: Recipe? = null,
    val errorMessage: String? = null
)
