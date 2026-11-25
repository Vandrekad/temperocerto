package br.edu.orlandovandres.temperocerto.domain.usecase

import br.edu.orlandovandres.temperocerto.domain.model.CatalogIngredient
import br.edu.orlandovandres.temperocerto.domain.repository.RecipeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveIngredientCatalogUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(): Flow<List<CatalogIngredient>> = repository.observeIngredientCatalog()
}
