package br.edu.orlandovandres.temperocerto.presentation.combiner

import androidx.compose.runtime.Immutable
import br.edu.orlandovandres.temperocerto.domain.model.CatalogIngredient
import br.edu.orlandovandres.temperocerto.domain.model.CombinationMatch
import br.edu.orlandovandres.temperocerto.domain.model.IngredientKind
import br.edu.orlandovandres.temperocerto.domain.model.SpicePairingSuggestion

enum class CatalogFilterTab(val label: String, val kind: IngredientKind?) {
    SPICES("Temperos", IngredientKind.SPICE_OR_SEASONING),
    SAUCES("Molhos", IngredientKind.SAUCE_OR_CONDIMENT),
    PROTEINS("Proteínas", IngredientKind.PROTEIN),
    VEGETABLES("Vegetais", IngredientKind.VEGETABLE),
    ALL("Todos", null)
}

@Immutable
data class CombinerUiState(
    val isLoadingCatalog: Boolean = true,
    val isSearchingMatches: Boolean = false,
    val isOnline: Boolean = true,
    val catalogQuery: String = "",
    val selectedTab: CatalogFilterTab = CatalogFilterTab.SPICES,
    val selectedItems: List<String> = emptyList(),
    val availableCatalog: List<CatalogIngredient> = emptyList(),
    val suggestedPairings: List<SpicePairingSuggestion> = emptyList(),
    val matches: List<CombinationMatch> = emptyList(),
    val errorMessage: String? = null
)
