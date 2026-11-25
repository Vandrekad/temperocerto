package br.edu.orlandovandres.temperocerto.domain.model

/**
 * Classificação culinária dos ingredientes para separar temperos/aromáticos dos itens base.
 */
enum class IngredientKind(val labelPt: String) {
    SPICE_OR_SEASONING("Temperos & Especiarias"),
    SAUCE_OR_CONDIMENT("Molhos & Condimentos"),
    PROTEIN("Proteínas"),
    VEGETABLE("Vegetais & Raízes"),
    GRAIN_OR_PASTA("Grãos & Massas"),
    DAIRY("Laticínios"),
    OTHER("Outros")
}
