package br.edu.orlandovandres.temperocerto.data.remote.dto

import com.google.gson.annotations.SerializedName

data class IngredientDto(
    @SerializedName("idIngredient") val idIngredient: String?,
    @SerializedName("strIngredient") val strIngredient: String?,
    @SerializedName("strDescription") val strDescription: String?,
    @SerializedName("strType") val strType: String?,
    @SerializedName("strThumb") val strThumb: String?
)

data class IngredientListResponseDto(
    @SerializedName("meals") val meals: List<IngredientDto>?
)
