package br.edu.orlandovandres.temperocerto.domain.model

data class Recipe(
    val id: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val thumbnailUrl: String?,
    val tags: List<String>,
    val youtubeUrl: String?,
    val ingredients: List<IngredientMeasure>,
    val isFavorite: Boolean,
    val hasFullDetails: Boolean,
    val cachedAt: Long
) {
    val spicesAndSeasonings: List<IngredientMeasure>
        get() = ingredients.filter { it.isSpiceOrSeasoning }

    val baseIngredients: List<IngredientMeasure>
        get() = ingredients.filterNot { it.isSpiceOrSeasoning }
}
