package br.edu.orlandovandres.temperocerto.domain.usecase

import br.edu.orlandovandres.temperocerto.domain.model.Recipe
import br.edu.orlandovandres.temperocerto.domain.repository.RecipeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetRecipeDetailUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    fun observe(id: String): Flow<Recipe?> = repository.observeRecipeById(id)

    suspend fun refresh(id: String, forceRefresh: Boolean = false): Result<Recipe> {
        return repository.getRecipeDetail(id, forceRefresh)
    }
}
