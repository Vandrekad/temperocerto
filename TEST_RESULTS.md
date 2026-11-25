# Relatório de Testes Unitários — TemperoCerto (`TEST_RESULTS.md`)

**Autor:** Orlando Vandres — **RA:** 170197  
**Aplicação:** `br.edu.orlandovandres.temperocerto`  
**Comando Executado:** `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`  
**Ambiente:** Windows 11 • OpenJDK 21.0.10 (Android Studio JBR) • Gradle 8.11.1 • Kotlin 2.0.21  
**Resultado Geral:** **18/18 testes aprovados (100% PASSED — 0 falhas, 0 erros, 0 ignorados)**

---

## Resumo por Suíte de Testes

| Suíte de Testes | Casos | Falhas | Erros | Ignorados | Tempo (s) | Status |
|---|---:|---:|---:|---:|---:|---|
| `RecipeMapperTest` | 2 | 0 | 0 | 0 | 0.184s | PASSED |
| `OfflineFirstRecipeRepositoryTest` | 4 | 0 | 0 | 0 | 1.360s | PASSED |
| `FindCombinationsUseCaseTest` | 4 | 0 | 0 | 0 | 0.098s | PASSED |
| `RecipeUseCasesTest` | 3 | 0 | 0 | 0 | 0.091s | PASSED |
| `SuggestSpicePairingsUseCaseTest` | 1 | 0 | 0 | 0 | 0.002s | PASSED |
| `CombinerViewModelTest` | 2 | 0 | 0 | 0 | 3.209s | PASSED |
| `RecipesViewModelTest` | 2 | 0 | 0 | 0 | 0.247s | PASSED |
| **Total** | **18** | **0** | **0** | **0** | **5.191s** | **PASSED** |

---

## Detalhamento dos Casos de Teste Executados

### 1. Camada de Domínio (`domain/`)

- **`FindCombinationsUseCaseTest`**
  1. `returns empty list when selected items are empty or blank` — **PASSED** (`0.025s`): valida que entradas vazias ou apenas espaços não consultam desnecessariamente o repositório e retornam lista vazia com sucesso.
  2. `ranks full matches before partial matches and prioritizes favorites` — **PASSED** (`0.031s`): valida o cálculo de `matchScore`, identificação de `extraSpicesInRecipe`, `missingSelectedItems` e ordenação por compatibilidade -> quantidade de itens -> favorito.
  3. `matches case-insensitively and by culinary substring` — **PASSED** (`0.028s`): valida correspondência tolerante (ex.: `"garlic"` casa com `"fresh minced garlic"`).
  4. `propagates failure when repository returns error` — **PASSED** (`0.013s`): valida propagação segura de `Result.failure`.

- **`SuggestSpicePairingsUseCaseTest`**
  5. `ranks co-occurring spices descending and excludes already selected or neutral items` — **PASSED** (`0.002s`): valida contagem de coocorrência de temperos entre receitas, ordenação decrescente e exclusão de itens já selecionados ou neutros (`Salt`, `Water`).

- **`RecipeUseCasesTest`**
  6. `SearchRecipesUseCase trims query before calling repository` — **PASSED** (`0.053s`): valida higienização de espaços antes da busca.
  7. `ToggleFavoriteRecipeUseCase delegates to repository` — **PASSED** (`0.037s`): valida delegação correta de alternância de favorito.
  8. `SpiceClassifier categorizes spices, sauces, proteins and vegetables accurately` — **PASSED** (`0.001s`): valida classificação semântica entre especiarias, molhos, proteínas, vegetais e itens não sugeríveis.

### 2. Camada de Dados (`data/`)

- **`RecipeMapperTest`**
  9. `dtoToEntity extracts valid ingredients, ignores blank slots and preserves favorite on round-trip` — **PASSED** (`0.044s`): valida extração limpa dos 20 slots da API TheMealDB e preservação de `isFavorite` no ciclo `DTO -> Entity -> Domain`.
  10. `dtoToEntity returns null when id or name is missing` — **PASSED** (`0.140s`): valida proteção contra registros malformados da API.

- **`OfflineFirstRecipeRepositoryTest`**
  11. `syncCatalog preserves local favorite flag when upserting remote recipes` — **PASSED** (`0.686s`): valida que o *upsert* após sincronização remota preserva `isFavorite = true` das receitas já favoritadas no Room.
  12. `searchRecipes falls back to local Room data without crash when remote fails` — **PASSED** (`0.061s`): valida resiliência e *fallback* para dados locais quando a API lança `IOException`.
  13. `getCandidateRecipesForIngredients returns local recipes immediately when offline` — **PASSED** (`0.095s`): valida que em modo offline o repositório não tenta chamadas remotas e entrega imediatamente o acervo local.
  14. `toggleFavorite flips boolean flag in DAO` — **PASSED** (`0.516s`): valida atualização do estado de favorito no Room.

### 3. Camada de Apresentação (`presentation/`)

- **`CombinerViewModelTest`**
  15. `adding and removing items updates selectedItems, matches and suggested pairings` — **PASSED** (`0.078s`): valida via Turbine as emissões de `StateFlow` ao adicionar/remover temperos, calcular receitas compatíveis e atualizar sugestões de harmonização.
  16. `reflects offline connectivity state banner accurately` — **PASSED** (`3.129s`): valida transição reativa de `isOnline` (`true -> false`) no estado da UI.

- **`RecipesViewModelTest`**
  17. `filters recipes by category and query via StateFlow` — **PASSED** (`0.126s`): valida filtragem por categoria e busca textual sobre o `StateFlow`.
  18. `exposes error message when forced sync fails while keeping cached recipes` — **PASSED** (`0.121s`): valida exibição controlada de erro mantendo o catálogo em cache visível.

---

## Observações da Validação

- Uma falha inicial ocorreu durante o desenvolvimento no teste `CombinerViewModelTest > adding and removing items updates selectedItems, matches and suggested pairings` devido à coleta de uma emissão intermediária do `StateFlow` combinada (`selectedItems` atualizado antes da conclusão assíncrona do cálculo de `matches`). O teste foi ajustado com `expectMostRecentItem()` e reexecutado, passando com 100% de aprovação.
- Todos os relatórios XML gerados pelo Gradle estão disponíveis em `app/build/test-results/testDebugUnitTest/`.
