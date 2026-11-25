# Decomposição de Tarefas — TemperoCerto (SDD - Etapa 3)

**Autor:** Orlando Vandres — **RA:** 170197  
**Referências:** [docs/01-especificacao.md](file:///c:/Users/orlan/Projects/orlando-sdd/docs/01-especificacao.md) | [docs/02-planejamento.md](file:///c:/Users/orlan/Projects/orlando-sdd/docs/02-planejamento.md)

---

## Sequência Incremental de Execução (T01 a T11)

| ID | Tarefa | Dependências | Entregáveis Principais | Critério de Aceite |
|---|---|---|---|---|
| **T01** | Especificação SDD e inicialização Git | — | `docs/01-especificacao.md`, branch `main` | Requisitos funcionais, não funcionais e regras de negócio documentados; Commit 1 registrado. |
| **T02** | Planejamento arquitetural e decomposição | T01 | `docs/02-planejamento.md`, `docs/03-decomposicao.md` | Clean Architecture, API TheMealDB, estratégia Offline First e backlog de tarefas definidos; Commit 2 registrado. |
| **T03** | Estrutura Android, Gradle Wrapper e Hilt | T02 | `gradle/libs.versions.toml`, `gradlew`, `build.gradle.kts`, `TemperoCertoApplication`, `MainActivity`, `DispatcherModule`, tema Material 3 e recursos | Projeto configurado com AGP 8.7.3, Kotlin 2.0.21, Hilt 2.52 e Gradle 8.11.1; Commit 3 registrado. |
| **T04** | Camada de Domínio (`domain/`) | T03 | Modelos, `SpiceClassifier`, interfaces `RecipeRepository`/`ConnectivityObserver` e 9 `UseCase`s | Zero acoplamento com Android/Room/Retrofit; lógica de `matchScore` e coocorrência implementada; Commit 4 registrado. |
| **T05** | Persistência local com Room (`data/local/`) | T04 | `RecipeEntity`, `IngredientCatalogEntity`, DAOs, `TemperoDatabase`, `LocalSeedData` | Schema Room definido com índices e dados iniciais offline; Commit 5 registrado. |
| **T06** | Consumo REST com Retrofit (`data/remote/`, `mapper/`) | T04, T05 | `TheMealDbApi`, DTOs (`MealDto`, `FilteredMealDto`, `IngredientDto`), `RecipeMapper`, `IngredientMapper` | Endpoints mapeados e extração limpa de até 20 pares ingrediente/medida; Commit 6 registrado. |
| **T07** | Repositório Offline First, conectividade e DI | T05, T06 | `OfflineFirstRecipeRepository`, `NetworkConnectivityObserver`, módulos Hilt (`Database`, `Network`, `Repository`) | `compileDebugKotlin` passa sem erros; preservação de favoritos e fallback offline garantidos; Commit 7 registrado. |
| **T08** | Telas de Receitas e Detalhes (Compose) | T07 | `OfflineBanner`, `StateViews`, `RecipeCard`, `RecipesScreen`, `RecipeDetailScreen` + ViewModels | Estados Loading/Success/Empty/Error tratados; separação visual entre Temperos e Ingredientes Base; Commit 8 registrado. |
| **T09** | Combinador, Meu Livro (Favoritos) e Navegação | T08 | `CombinerScreen`, `FavoritesScreen`, `Screen`, `TemperoCertoAppRoot`, integração `MainActivity` | Seleção multi-item, sugestões de harmonização, favoritos offline e `compileDebugKotlin` verde; Commit 9 registrado. |
| **T10** | Testes unitários automatizados | T09 | Suíte em `app/src/test/` (Domain, Mapper, Repository, ViewModels) | `testDebugUnitTest` com 100% de aprovação; Commit 10 registrado. |
| **T11** | APK, evidências no emulador e documentação final | T10 | `apk/TemperoCerto-debug.apk`, screenshots online/offline em `docs/evidencias/`, `TEST_RESULTS.md`, `README.md` | `assembleDebug` verde, app validado online e offline no emulador, documentação completa; Commit 11 registrado. |
