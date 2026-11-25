package br.edu.orlandovandres.temperocerto.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.orlandovandres.temperocerto.domain.repository.ConnectivityObserver
import br.edu.orlandovandres.temperocerto.domain.usecase.ObserveFavoriteRecipesUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.ToggleFavoriteRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    observeFavoriteRecipesUseCase: ObserveFavoriteRecipesUseCase,
    private val toggleFavoriteRecipeUseCase: ToggleFavoriteRecipeUseCase,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")

    val uiState: StateFlow<FavoritesUiState> = combine(
        observeFavoriteRecipesUseCase(),
        connectivityObserver.isOnline,
        queryFlow
    ) { favorites, isOnline, query ->
        val normalized = query.trim().lowercase(Locale.ROOT)
        val filtered = if (normalized.isEmpty()) {
            favorites
        } else {
            favorites.filter { recipe ->
                recipe.name.lowercase(Locale.ROOT).contains(normalized) ||
                    recipe.ingredients.any { it.name.lowercase(Locale.ROOT).contains(normalized) }
            }
        }

        FavoritesUiState(
            isLoading = false,
            isOnline = isOnline,
            searchQuery = query,
            favorites = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState()
    )

    fun onSearchQueryChange(query: String) {
        queryFlow.value = query
    }

    fun onToggleFavorite(recipeId: String) {
        viewModelScope.launch {
            toggleFavoriteRecipeUseCase(recipeId)
        }
    }
}
