package br.edu.orlandovandres.temperocerto.data.mapper

import br.edu.orlandovandres.temperocerto.data.local.entity.IngredientCatalogEntity
import br.edu.orlandovandres.temperocerto.data.remote.dto.IngredientDto
import br.edu.orlandovandres.temperocerto.domain.model.CatalogIngredient
import br.edu.orlandovandres.temperocerto.domain.model.IngredientKind
import br.edu.orlandovandres.temperocerto.domain.model.SpiceClassifier
import javax.inject.Inject

class IngredientMapper @Inject constructor() {

    fun dtoToEntity(
        dto: IngredientDto,
        now: Long = System.currentTimeMillis()
    ): IngredientCatalogEntity? {
        val id = dto.idIngredient?.trim().orEmpty()
        val name = dto.strIngredient?.trim().orEmpty()
        if (id.isEmpty() || name.isEmpty()) return null

        val kind = SpiceClassifier.classify(name = name, rawType = dto.strType)
        val thumb = dto.strThumb?.trim()?.takeIf { it.isNotEmpty() }
            ?: "https://www.themealdb.com/images/ingredients/${name.lowercase().replace(" ", "%20")}.png"

        return IngredientCatalogEntity(
            id = id,
            name = name,
            description = dto.strDescription?.trim()?.takeIf { it.isNotEmpty() },
            kind = kind.name,
            thumbnailUrl = thumb,
            cachedAt = now
        )
    }

    fun entityToDomain(entity: IngredientCatalogEntity): CatalogIngredient {
        val kind = runCatching { IngredientKind.valueOf(entity.kind) }
            .getOrDefault(SpiceClassifier.classify(entity.name))

        return CatalogIngredient(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            kind = kind,
            thumbnailUrl = entity.thumbnailUrl
        )
    }
}
