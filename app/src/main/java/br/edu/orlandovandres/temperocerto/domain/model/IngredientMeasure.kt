package br.edu.orlandovandres.temperocerto.domain.model

data class IngredientMeasure(
    val name: String,
    val measure: String,
    val kind: IngredientKind = SpiceClassifier.classify(name)
) {
    val isSpiceOrSeasoning: Boolean
        get() = SpiceClassifier.isSpiceOrCondiment(kind)
}
