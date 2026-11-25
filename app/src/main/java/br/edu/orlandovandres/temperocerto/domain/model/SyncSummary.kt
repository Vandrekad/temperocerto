package br.edu.orlandovandres.temperocerto.domain.model

data class SyncSummary(
    val recipesSynced: Int,
    val ingredientsSynced: Int,
    val fromCacheOnly: Boolean
)
