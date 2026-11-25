package br.edu.orlandovandres.temperocerto.domain.usecase

import br.edu.orlandovandres.temperocerto.domain.model.CombinationMatch
import br.edu.orlandovandres.temperocerto.domain.model.SpiceClassifier
import br.edu.orlandovandres.temperocerto.domain.repository.RecipeRepository
import java.util.Locale
import javax.inject.Inject

/**
 * Calcula combinações de receitas compatíveis com os temperos e ingredientes selecionados,
 * aplicando correspondência flexível case-insensitive e ranqueamento por score de compatibilidade.
 */
class FindCombinationsUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(selectedItems: List<String>): Result<List<CombinationMatch>> {
        val cleanedSelection = selectedItems
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase(Locale.ROOT) }

        if (cleanedSelection.isEmpty()) {
            return Result.success(emptyList())
        }

        return repository.getCandidateRecipesForIngredients(cleanedSelection).map { candidates ->
            val selectionNormalized = cleanedSelection.map { it to it.lowercase(Locale.ROOT) }

            candidates.mapNotNull { recipe ->
                val recipeIngredientNames = recipe.ingredients.map {
                    it.name to it.name.lowercase(Locale.ROOT)
                }
                if (recipeIngredientNames.isEmpty()) return@mapNotNull null

                val matched = mutableListOf<String>()
                val missing = mutableListOf<String>()

                for ((originalSelected, lowerSelected) in selectionNormalized) {
                    val hasMatch = recipeIngredientNames.any { (_, lowerRecipeItem) ->
                        lowerRecipeItem == lowerSelected ||
                            lowerRecipeItem.contains(lowerSelected) ||
                            lowerSelected.contains(lowerRecipeItem)
                    }
                    if (hasMatch) {
                        matched += originalSelected
                    } else {
                        missing += originalSelected
                    }
                }

                if (matched.isEmpty()) return@mapNotNull null

                val extraSpices = recipe.spicesAndSeasonings
                    .map { SpiceClassifier.normalizeSpiceName(it.name) }
                    .distinct()
                    .filter { spiceName ->
                        val lowerSpice = spiceName.lowercase(Locale.ROOT)
                        selectionNormalized.none { (_, lowerSelected) ->
                            lowerSpice == lowerSelected ||
                                lowerSpice.contains(lowerSelected) ||
                                lowerSelected.contains(lowerSpice)
                        }
                    }

                val score = matched.size.toDouble() / cleanedSelection.size.toDouble()
                CombinationMatch(
                    recipe = recipe,
                    matchedItems = matched,
                    missingSelectedItems = missing,
                    extraSpicesInRecipe = extraSpices,
                    matchScore = score,
                    isFullMatch = matched.size == cleanedSelection.size
                )
            }.sortedWith(
                compareByDescending<CombinationMatch> { it.matchScore }
                    .thenByDescending { it.matchedItems.size }
                    .thenByDescending { it.recipe.isFavorite }
                    .thenBy { it.recipe.name.lowercase(Locale.ROOT) }
            )
        }
    }
}
