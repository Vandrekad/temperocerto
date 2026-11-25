package br.edu.orlandovandres.temperocerto.data.mapper

import br.edu.orlandovandres.temperocerto.data.local.entity.RecipeEntity
import br.edu.orlandovandres.temperocerto.data.remote.dto.MealDto
import br.edu.orlandovandres.temperocerto.domain.model.IngredientMeasure
import br.edu.orlandovandres.temperocerto.domain.model.Recipe
import br.edu.orlandovandres.temperocerto.domain.model.SpiceClassifier
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class RecipeMapper @Inject constructor(
    private val gson: Gson
) {
    private val ingredientListType = object : TypeToken<List<IngredientMeasure>>() {}.type

    fun dtoToEntity(
        dto: MealDto,
        isFavorite: Boolean = false,
        now: Long = System.currentTimeMillis()
    ): RecipeEntity? {
        val id = dto.idMeal?.trim().orEmpty()
        val name = dto.strMeal?.trim().orEmpty()
        if (id.isEmpty() || name.isEmpty()) return null

        val ingredients = dto.extractIngredients().map { (ingName, measure) ->
            IngredientMeasure(
                name = ingName,
                measure = measure,
                kind = SpiceClassifier.classify(ingName)
            )
        }

        val tags = dto.strTags
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()

        return RecipeEntity(
            id = id,
            name = name,
            category = dto.strCategory?.trim().orEmpty().ifEmpty { "Variados" },
            area = dto.strArea?.trim().orEmpty().ifEmpty { "Internacional" },
            instructions = dto.strInstructions?.trim().orEmpty(),
            thumbnailUrl = dto.strMealThumb?.trim()?.takeIf { it.isNotEmpty() },
            tagsCsv = tags.joinToString(","),
            youtubeUrl = dto.strYoutube?.trim()?.takeIf { it.isNotEmpty() },
            ingredientsJson = gson.toJson(ingredients),
            isFavorite = isFavorite,
            hasFullDetails = ingredients.isNotEmpty() || !dto.strInstructions.isNullOrBlank(),
            cachedAt = now
        )
    }

    fun entityToDomain(entity: RecipeEntity): Recipe {
        val parsedIngredients: List<IngredientMeasure> = runCatching {
            gson.fromJson<List<IngredientMeasure>>(entity.ingredientsJson, ingredientListType)
        }.getOrNull().orEmpty().map { item ->
            item.copy(kind = SpiceClassifier.classify(item.name))
        }

        val tags = entity.tagsCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        return Recipe(
            id = entity.id,
            name = entity.name,
            category = entity.category,
            area = entity.area,
            instructions = entity.instructions,
            thumbnailUrl = entity.thumbnailUrl,
            tags = tags,
            youtubeUrl = entity.youtubeUrl,
            ingredients = parsedIngredients,
            isFavorite = entity.isFavorite,
            hasFullDetails = entity.hasFullDetails,
            cachedAt = entity.cachedAt
        )
    }
}
