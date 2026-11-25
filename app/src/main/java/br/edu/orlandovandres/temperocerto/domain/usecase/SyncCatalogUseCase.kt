package br.edu.orlandovandres.temperocerto.domain.usecase

import br.edu.orlandovandres.temperocerto.domain.model.SyncSummary
import br.edu.orlandovandres.temperocerto.domain.repository.RecipeRepository
import javax.inject.Inject

class SyncCatalogUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(force: Boolean = false): Result<SyncSummary> {
        return repository.syncCatalog(force)
    }
}
