package br.edu.orlandovandres.temperocerto.domain.model

data class CatalogIngredient(
    val id: String,
    val name: String,
    val description: String?,
    val kind: IngredientKind,
    val thumbnailUrl: String?
)
