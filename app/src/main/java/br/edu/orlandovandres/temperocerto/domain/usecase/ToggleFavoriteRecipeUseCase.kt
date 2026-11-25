package br.edu.orlandovandres.temperocerto.domain.usecase

import br.edu.orlandovandres.temperocerto.domain.repository.RecipeRepository
import javax.inject.Inject

class ToggleFavoriteRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: String): Result<Boolean> {
        return repository.toggleFavorite(recipeId)
    }
}
