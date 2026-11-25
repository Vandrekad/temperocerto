package br.edu.orlandovandres.temperocerto.presentation.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.orlandovandres.temperocerto.domain.repository.ConnectivityObserver
import br.edu.orlandovandres.temperocerto.domain.usecase.ObserveRecipesUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.SearchRecipesUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.SyncCatalogUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.ToggleFavoriteRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RecipesViewModel @Inject constructor(
    observeRecipesUseCase: ObserveRecipesUseCase,
    private val searchRecipesUseCase: SearchRecipesUseCase,
    private val toggleFavoriteRecipeUseCase: ToggleFavoriteRecipeUseCase,
    private val syncCatalogUseCase: SyncCatalogUseCase,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val searchQueryFlow = MutableStateFlow("")
    private val selectedCategoryFlow = MutableStateFlow(RecipesUiState.ALL_CATEGORIES)
    private val isSyncingFlow = MutableStateFlow(false)
    private val hasInitialLoadCompletedFlow = MutableStateFlow(false)
    private val errorMessageFlow = MutableStateFlow<String?>(null)

    private var searchJob: Job? = null

    val uiState: StateFlow<RecipesUiState> = combine(
        observeRecipesUseCase(),
        connectivityObserver.isOnline,
        searchQueryFlow,
        selectedCategoryFlow,
        combine(isSyncingFlow, hasInitialLoadCompletedFlow, errorMessageFlow) { syncing, loaded, err ->
            Triple(syncing, loaded, err)
        }
    ) { allRecipes, isOnline, query, selectedCategory, syncMeta ->
        val (isSyncing, hasLoaded, errorMessage) = syncMeta
        val categories = buildList {
            add(RecipesUiState.ALL_CATEGORIES)
            addAll(
                allRecipes.map { it.category }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
            )
        }

        val normalizedQuery = query.trim().lowercase(Locale.ROOT)
        val filtered = allRecipes.filter { recipe ->
            val matchesCategory = selectedCategory == RecipesUiState.ALL_CATEGORIES ||
                recipe.category.equals(selectedCategory, ignoreCase = true)

            val matchesQuery = normalizedQuery.isEmpty() ||
                recipe.name.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                recipe.category.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                recipe.area.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                recipe.ingredients.any { it.name.lowercase(Locale.ROOT).contains(normalizedQuery) }

            matchesCategory && matchesQuery
        }

        RecipesUiState(
            isLoading = !hasLoaded && allRecipes.isEmpty(),
            isSyncing = isSyncing,
            isOnline = isOnline,
            searchQuery = query,
            selectedCategory = selectedCategory,
            categories = categories,
            recipes = filtered,
            errorMessage = errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipesUiState()
    )

    init {
        syncCatalog(force = false)
    }

    fun onSearchQueryChange(query: String) {
        searchQueryFlow.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            if (query.trim().length >= 2) {
                searchRecipesUseCase(query).onFailure { error ->
                    errorMessageFlow.value = error.localizedMessage
                        ?: "Não foi possível consultar a base online."
                }
            }
        }
    }

    fun onCategorySelected(category: String) {
        selectedCategoryFlow.value = category
    }

    fun onRefresh() {
        syncCatalog(force = true)
    }

    fun onToggleFavorite(recipeId: String) {
        viewModelScope.launch {
            toggleFavoriteRecipeUseCase(recipeId).onFailure { error ->
                errorMessageFlow.value = error.localizedMessage ?: "Erro ao atualizar favorito."
            }
        }
    }

    fun onDismissError() {
        errorMessageFlow.value = null
    }

    private fun syncCatalog(force: Boolean) {
        viewModelScope.launch {
            isSyncingFlow.value = true
            errorMessageFlow.value = null
            syncCatalogUseCase(force = force).onFailure { error ->
                errorMessageFlow.value = error.localizedMessage
                    ?: "Falha na sincronização com a base online."
            }
            isSyncingFlow.value = false
            hasInitialLoadCompletedFlow.value = true
        }
    }
}
