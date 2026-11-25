package br.edu.orlandovandres.temperocerto.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FilteredMealDto(
    @SerializedName("idMeal") val idMeal: String?,
    @SerializedName("strMeal") val strMeal: String?,
    @SerializedName("strMealThumb") val strMealThumb: String?
)

data class FilteredMealListResponseDto(
    @SerializedName("meals") val meals: List<FilteredMealDto>?
)
