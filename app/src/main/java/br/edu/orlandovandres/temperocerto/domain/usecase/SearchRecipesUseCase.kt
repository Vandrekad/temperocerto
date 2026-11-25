package br.edu.orlandovandres.temperocerto.domain.usecase

import br.edu.orlandovandres.temperocerto.domain.model.Recipe
import br.edu.orlandovandres.temperocerto.domain.repository.RecipeRepository
import javax.inject.Inject

class SearchRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(query: String): Result<List<Recipe>> {
        return repository.searchRecipes(query.trim())
    }
}
