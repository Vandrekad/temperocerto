package br.edu.orlandovandres.temperocerto.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipes",
    indices = [
        Index(value = ["name"]),
        Index(value = ["category"]),
        Index(value = ["isFavorite"])
    ]
)
data class RecipeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val thumbnailUrl: String?,
    val tagsCsv: String,
    val youtubeUrl: String?,
    val ingredientsJson: String,
    val isFavorite: Boolean,
    val hasFullDetails: Boolean,
    val cachedAt: Long
)
