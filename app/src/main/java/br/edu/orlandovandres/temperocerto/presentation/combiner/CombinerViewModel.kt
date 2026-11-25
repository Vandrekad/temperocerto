package br.edu.orlandovandres.temperocerto.presentation.combiner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.orlandovandres.temperocerto.domain.model.CombinationMatch
import br.edu.orlandovandres.temperocerto.domain.repository.ConnectivityObserver
import br.edu.orlandovandres.temperocerto.domain.usecase.FindCombinationsUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.ObserveIngredientCatalogUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.ObserveRecipesUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.SuggestSpicePairingsUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.SyncCatalogUseCase
import br.edu.orlandovandres.temperocerto.domain.usecase.ToggleFavoriteRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CombinerViewModel @Inject constructor(
    observeIngredientCatalogUseCase: ObserveIngredientCatalogUseCase,
    observeRecipesUseCase: ObserveRecipesUseCase,
    private val findCombinationsUseCase: FindCombinationsUseCase,
    private val suggestSpicePairingsUseCase: SuggestSpicePairingsUseCase,
    private val toggleFavoriteRecipeUseCase: ToggleFavoriteRecipeUseCase,
    private val syncCatalogUseCase: SyncCatalogUseCase,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val catalogQueryFlow = MutableStateFlow("")
    private val selectedTabFlow = MutableStateFlow(CatalogFilterTab.SPICES)
    private val selectedItemsFlow = MutableStateFlow<List<String>>(emptyList())
    private val matchesFlow = MutableStateFlow<List<CombinationMatch>>(emptyList())
    private val isSearchingFlow = MutableStateFlow(false)
    private val errorMessageFlow = MutableStateFlow<String?>(null)

    private var searchMatchesJob: Job? = null

    val uiState: StateFlow<CombinerUiState> = combine(
        observeIngredientCatalogUseCase(),
        observeRecipesUseCase(),
        connectivityObserver.isOnline,
        combine(catalogQueryFlow, selectedTabFlow, selectedItemsFlow) { q, tab, sel ->
            Triple(q, tab, sel)
        },
        combine(matchesFlow, isSearchingFlow, errorMessageFlow) { m, searching, err ->
            Triple(m, searching, err)
        }
    ) { catalog, allRecipes, isOnline, filterState, resultState ->
        val (query, tab, selectedItems) = filterState
        val (matches, isSearching, errorMessage) = resultState

        val selectedLower = selectedItems.map { it.lowercase(Locale.ROOT) }.toSet()
        val normalizedQuery = query.trim().lowercase(Locale.ROOT)

        val filteredCatalog = catalog.filter { item ->
            val matchesTab = tab.kind == null || item.kind == tab.kind
            val matchesQuery = normalizedQuery.isEmpty() ||
                item.name.lowercase(Locale.ROOT).contains(normalizedQuery)
            val notSelected = item.name.lowercase(Locale.ROOT) !in selectedLower
            matchesTab && matchesQuery && notSelected
        }.take(40)

        val pairingBaseRecipes = if (selectedItems.isNotEmpty() && matches.isNotEmpty()) {
            matches.map { it.recipe }
        } else {
            allRecipes
        }

        val suggestions = suggestSpicePairingsUseCase(
            recipes = pairingBaseRecipes,
            selectedItems = selectedItems,
            limit = 10
        )

        CombinerUiState(
            isLoadingCatalog = catalog.isEmpty(),
            isSearchingMatches = isSearching,
            isOnline = isOnline,
            catalogQuery = query,
            selectedTab = tab,
            selectedItems = selectedItems,
            availableCatalog = filteredCatalog,
            suggestedPairings = suggestions,
            matches = matches,
            errorMessage = errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CombinerUiState()
    )

    init {
        viewModelScope.launch {
            syncCatalogUseCase(force = false)
        }
    }

    fun onCatalogQueryChange(query: String) {
        catalogQueryFlow.value = query
    }

    fun onTabSelected(tab: CatalogFilterTab) {
        selectedTabFlow.value = tab
    }

    fun onAddItem(itemName: String) {
        val cleaned = itemName.trim()
        if (cleaned.isEmpty()) return
        val current = selectedItemsFlow.value
        if (current.any { it.equals(cleaned, ignoreCase = true) }) return

        val updated = current + cleaned
        selectedItemsFlow.value = updated
        recalculateMatches(updated)
    }

    fun onRemoveItem(itemName: String) {
        val updated = selectedItemsFlow.value.filterNot {
            it.equals(itemName, ignoreCase = true)
        }
        selectedItemsFlow.value = updated
        recalculateMatches(updated)
    }

    fun onClearSelection() {
        selectedItemsFlow.value = emptyList()
        matchesFlow.value = emptyList()
        errorMessageFlow.value = null
        searchMatchesJob?.cancel()
        isSearchingFlow.value = false
    }

    fun onRetrySearch() {
        recalculateMatches(selectedItemsFlow.value)
    }

    fun onToggleFavorite(recipeId: String) {
        viewModelScope.launch {
            toggleFavoriteRecipeUseCase(recipeId).onSuccess { newFavorite ->
                matchesFlow.value = matchesFlow.value.map { match ->
                    if (match.recipe.id == recipeId) {
                        match.copy(recipe = match.recipe.copy(isFavorite = newFavorite))
                    } else {
                        match
                    }
                }
            }
        }
    }

    private fun recalculateMatches(items: List<String>) {
        searchMatchesJob?.cancel()
        if (items.isEmpty()) {
            matchesFlow.value = emptyList()
            isSearchingFlow.value = false
            errorMessageFlow.value = null
            return
        }

        searchMatchesJob = viewModelScope.launch {
            isSearchingFlow.value = true
            errorMessageFlow.value = null
            findCombinationsUseCase(items)
                .onSuccess { result ->
                    matchesFlow.value = result
                }
                .onFailure { error ->
                    errorMessageFlow.value = error.localizedMessage
                        ?: "Erro ao calcular combinações de temperos."
                }
            isSearchingFlow.value = false
        }
    }
}
