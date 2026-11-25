package br.edu.orlandovandres.temperocerto.data.remote.api

import br.edu.orlandovandres.temperocerto.data.remote.dto.FilteredMealListResponseDto
import br.edu.orlandovandres.temperocerto.data.remote.dto.IngredientListResponseDto
import br.edu.orlandovandres.temperocerto.data.remote.dto.MealListResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMealDbApi {

    @GET("search.php")
    suspend fun searchMealsByName(@Query("s") query: String): MealListResponseDto

    @GET("search.php")
    suspend fun searchMealsByFirstLetter(@Query("f") letter: String): MealListResponseDto

    @GET("lookup.php")
    suspend fun lookupMealById(@Query("i") id: String): MealListResponseDto

    @GET("filter.php")
    suspend fun filterMealsByIngredient(@Query("i") ingredient: String): FilteredMealListResponseDto

    @GET("list.php?i=list")
    suspend fun listIngredients(): IngredientListResponseDto
}
