package br.edu.orlandovandres.temperocerto.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ingredient_catalog",
    indices = [
        Index(value = ["name"]),
        Index(value = ["kind"])
    ]
)
data class IngredientCatalogEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val kind: String,
    val thumbnailUrl: String?,
    val cachedAt: Long
)
