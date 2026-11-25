package br.edu.orlandovandres.temperocerto.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.orlandovandres.temperocerto.domain.repository.ConnectivityObserver
import br.edu.orlandovandres.temperocerto.domain.usecase.GetRecipeDetailUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.ToggleFavoriteRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRecipeDetailUseCase: GetRecipeDetailUseCase,
    private val toggleFavoriteRecipeUseCase: ToggleFavoriteRecipeUseCase,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val recipeId: String = checkNotNull(savedStateHandle["recipeId"])
    private val isRefreshingFlow = MutableStateFlow(true)
    private val errorMessageFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RecipeDetailUiState> = combine(
        getRecipeDetailUseCase.observe(recipeId),
        connectivityObserver.isOnline,
        isRefreshingFlow,
        errorMessageFlow
    ) { recipe, isOnline, isRefreshing, errorMessage ->
        RecipeDetailUiState(
            isLoading = isRefreshing && recipe == null,
            isOnline = isOnline,
            recipe = recipe,
            errorMessage = if (recipe == null) errorMessage else null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipeDetailUiState()
    )

    init {
        refresh(forceRefresh = false)
    }

    fun refresh(forceRefresh: Boolean = true) {
        viewModelScope.launch {
            isRefreshingFlow.value = true
            errorMessageFlow.value = null
            getRecipeDetailUseCase.refresh(recipeId, forceRefresh).onFailure { error ->
                errorMessageFlow.value = error.localizedMessage
                    ?: "Não foi possível carregar os detalhes desta receita."
            }
            isRefreshingFlow.value = false
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch {
            toggleFavoriteRecipeUseCase(recipeId)
        }
    }
}
