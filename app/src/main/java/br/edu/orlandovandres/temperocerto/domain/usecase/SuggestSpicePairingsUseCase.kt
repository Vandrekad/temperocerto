package br.edu.orlandovandres.temperocerto.domain.usecase

import br.edu.orlandovandres.temperocerto.domain.model.Recipe
import br.edu.orlandovandres.temperocerto.domain.model.SpiceClassifier
import br.edu.orlandovandres.temperocerto.domain.model.SpicePairingSuggestion
import java.util.Locale
import javax.inject.Inject

/**
 * Sugere temperos e especiarias que harmonizam com os itens selecionados,
 * calculando a coocorrência real nas receitas compatíveis (ou em todo o acervo).
 */
class SuggestSpicePairingsUseCase @Inject constructor() {

    operator fun invoke(
        recipes: List<Recipe>,
        selectedItems: List<String>,
        limit: Int = 10
    ): List<SpicePairingSuggestion> {
        if (recipes.isEmpty() || limit <= 0) return emptyList()

        val selectedLower = selectedItems
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { it.isNotEmpty() }

        val frequencyMap = linkedMapOf<String, MutableSet<String>>()

        for (recipe in recipes) {
            val distinctSpiceNames = recipe.spicesAndSeasonings
                .map { it.name }
                .filter { SpiceClassifier.isSuggestableSpice(it) }
                .map { SpiceClassifier.normalizeSpiceName(it) }
                .filter { it.isNotEmpty() }
                .distinctBy { it.lowercase(Locale.ROOT) }

            for (spice in distinctSpiceNames) {
                val lowerSpice = spice.lowercase(Locale.ROOT)
                val alreadySelected = selectedLower.any { sel ->
                    lowerSpice == sel || lowerSpice.contains(sel) || sel.contains(lowerSpice)
                }
                if (!alreadySelected) {
                    frequencyMap.getOrPut(spice) { linkedSetOf() }.add(recipe.id)
                }
            }
        }

        return frequencyMap.entries
            .map { (spiceName, recipeIds) ->
                SpicePairingSuggestion(
                    spiceName = spiceName,
                    recipeCount = recipeIds.size
                )
            }
            .sortedWith(
                compareByDescending<SpicePairingSuggestion> { it.recipeCount }
                    .thenBy { it.spiceName.lowercase(Locale.ROOT) }
            )
            .take(limit)
    }
}
