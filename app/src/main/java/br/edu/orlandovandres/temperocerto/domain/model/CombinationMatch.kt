package br.edu.orlandovandres.temperocerto.domain.model

data class CombinationMatch(
    val recipe: Recipe,
    val matchedItems: List<String>,
    val missingSelectedItems: List<String>,
    val extraSpicesInRecipe: List<String>,
    val matchScore: Double,
    val isFullMatch: Boolean
)
