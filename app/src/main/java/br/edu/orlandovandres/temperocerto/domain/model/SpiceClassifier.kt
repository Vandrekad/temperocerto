package br.edu.orlandovandres.temperocerto.domain.model

import java.util.Locale

/**
 * Classificador de domínio para distinguir especiarias, ervas, aromáticos e condimentos
 * de proteínas, vegetais e outros ingredientes base.
 */
object SpiceClassifier {

    private val spiceKeywords = setOf(
        "garlic", "ginger", "cumin", "paprika", "oregano", "thyme", "rosemary",
        "coriander", "cilantro", "parsley", "basil", "cinnamon", "turmeric",
        "curry", "pepper", "chili", "chilli", "cayenne", "nutmeg", "clove",
        "cloves", "bay leaf", "bay leaves", "cardamom", "fennel", "mint",
        "sage", "dill", "tarragon", "chives", "allspice", "saffron",
        "star anise", "fenugreek", "garam masala", "five spice", "ras el hanout",
        "zaatar", "sumac", "smoked paprika", "red pepper flakes", "marjoram",
        "lemongrass", "galangal", "wasabi", "horseradish", "vanilla",
        "alho", "gengibre", "cominho", "paprica", "oregano", "tomilho",
        "alecrim", "coentro", "salsa", "manjericao", "canela", "curcuma",
        "pimenta", "noz-moscada", "cravo", "louro", "hortela", "salvia",
        "seasoning", "spice", "herb", "mixed herbs", "italian seasoning",
        "cajun", "taco seasoning", "onion powder", "garlic powder", "celery salt"
    )

    private val sauceAndCondimentKeywords = setOf(
        "sauce", "soy sauce", "worcestershire", "vinegar", "balsamic",
        "mustard", "dijon", "olive oil", "sesame oil", "lemon", "lime",
        "honey", "maple syrup", "ketchup", "mayonnaise", "tahini",
        "miso", "fish sauce", "oyster sauce", "hoisin", "sriracha",
        "tabasco", "harissa", "pesto", "tomato paste", "tomato puree",
        "molho", "vinagre", "mostarda", "azeite", "limao", "mel"
    )

    private val proteinKeywords = setOf(
        "chicken", "beef", "pork", "salmon", "tuna", "fish", "shrimp",
        "prawn", "lamb", "turkey", "duck", "bacon", "sausage", "ham",
        "egg", "eggs", "tofu", "steak", "mince", "cod", "tilapia", "crab",
        "frango", "carne", "porco", "salmao", "peixe", "camarao", "ovo"
    )

    private val vegetableKeywords = setOf(
        "onion", "shallot", "scallion", "spring onion", "carrot", "potato",
        "sweet potato", "tomato", "bell pepper", "capsicum", "spinach",
        "broccoli", "cauliflower", "zucchini", "courgette", "eggplant",
        "aubergine", "mushroom", "celery", "leek", "peas", "beans",
        "lentil", "chickpea", "corn", "cabbage", "kale", "lettuce",
        "avocado", "cucumber", "pumpkin", "squash", "beetroot"
    )

    private val grainKeywords = setOf(
        "rice", "pasta", "spaghetti", "penne", "noodle", "noodles",
        "bread", "flour", "oat", "oats", "quinoa", "couscous",
        "tortilla", "macaroni", "lasagne", "fettuccine", "arroz", "macarrao"
    )

    private val dairyKeywords = setOf(
        "milk", "butter", "cheese", "parmesan", "cheddar", "mozzarella",
        "cream", "yogurt", "yoghurt", "ricotta", "feta", "sour cream",
        "ghee", "creme de leite", "queijo", "manteiga", "leite"
    )

    private val neutralNonSuggestableItems = setOf(
        "water", "boiling water", "cold water", "warm water", "hot water",
        "ice", "salt", "sea salt", "kosher salt", "table salt",
        "oil", "vegetable oil", "cooking oil", "sunflower oil", "canola oil",
        "sugar", "white sugar", "caster sugar", "granulated sugar",
        "flour", "plain flour", "all purpose flour", "self-raising flour"
    )

    fun classify(name: String, rawType: String? = null): IngredientKind {
        val typeNormalized = rawType?.trim()?.lowercase(Locale.ROOT).orEmpty()
        when (typeNormalized) {
            "seasoning", "spice", "herb" -> return IngredientKind.SPICE_OR_SEASONING
            "sauce", "dressing", "condiment" -> return IngredientKind.SAUCE_OR_CONDIMENT
            "meat", "fish", "seafood", "poultry" -> return IngredientKind.PROTEIN
            "vegetable", "root vegetable", "fruit", "legume", "bean" -> return IngredientKind.VEGETABLE
            "grain", "pasta", "bread", "rice", "cereal", "pastry" -> return IngredientKind.GRAIN_OR_PASTA
            "dairy", "cheese", "curd" -> return IngredientKind.DAIRY
        }

        val normalizedName = name.trim().lowercase(Locale.ROOT)
        if (matchesAny(normalizedName, spiceKeywords)) {
            return IngredientKind.SPICE_OR_SEASONING
        }
        if (matchesAny(normalizedName, sauceAndCondimentKeywords)) {
            return IngredientKind.SAUCE_OR_CONDIMENT
        }
        if (matchesAny(normalizedName, proteinKeywords)) {
            return IngredientKind.PROTEIN
        }
        if (matchesAny(normalizedName, vegetableKeywords)) {
            return IngredientKind.VEGETABLE
        }
        if (matchesAny(normalizedName, grainKeywords)) {
            return IngredientKind.GRAIN_OR_PASTA
        }
        if (matchesAny(normalizedName, dairyKeywords)) {
            return IngredientKind.DAIRY
        }
        return IngredientKind.OTHER
    }

    fun isSpiceOrCondiment(kind: IngredientKind): Boolean =
        kind == IngredientKind.SPICE_OR_SEASONING || kind == IngredientKind.SAUCE_OR_CONDIMENT

    fun normalizeSpiceName(raw: String): String {
        val cleaned = raw.trim().replace(Regex("\\s+"), " ")
        if (cleaned.isEmpty()) return ""
        return cleaned.split(" ").joinToString(" ") { word ->
            word.lowercase(Locale.ROOT).replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase(Locale.ROOT) else ch.toString()
            }
        }
    }

    fun isSuggestableSpice(name: String): Boolean {
        val normalized = name.trim().lowercase(Locale.ROOT)
        if (normalized.isEmpty() || normalized in neutralNonSuggestableItems) {
            return false
        }
        val kind = classify(normalized)
        return kind == IngredientKind.SPICE_OR_SEASONING || kind == IngredientKind.SAUCE_OR_CONDIMENT
    }

    private fun matchesAny(text: String, keywords: Set<String>): Boolean {
        if (text in keywords) return true
        return keywords.any { keyword ->
            text.contains(keyword)
        }
    }
}
