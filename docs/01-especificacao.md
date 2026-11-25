# Especificação do Projeto — TemperoCerto (SDD - Etapa 1)

**Autor:** Orlando Vandres — **RA:** 170197  
**Disciplina:** Desenvolvimento Android Moderno com Spec-Driven Development (SDD)  
**Aplicação:** TemperoCerto — Livro de Receitas Inteligente para Consulta e Harmonização de Temperos e Ingredientes  
**Package / Application ID:** `br.edu.orlandovandres.temperocerto`

---

## 1. Visão Geral e Problema Resolvido

Na rotina culinária, é comum ter alguns ingredientes e temperos disponíveis na cozinha e surgir a dúvida: *"Quais pratos consigo preparar com o que tenho agora?"* ou *"Quais especiarias e ervas harmonizam melhor com alho, frango ou gengibre?"*. Além disso, em muitas cozinhas ou situações com sinal instável, depender exclusivamente da internet inviabiliza a consulta rápida durante o preparo.

O **TemperoCerto** resolve esse problema funcionando como um **livro de receitas e guia prático de combinação de temperos e ingredientes** com arquitetura **Offline First**:
- Consulta uma base online pública (**TheMealDB API v1**) para enriquecer continuamente o catálogo de ingredientes, especiarias e receitas completas;
- Armazena receitas, medidas e catálogo localmente via **Room**, garantindo consulta, busca, combinação de temperos e acesso ao livro de favoritos mesmo sem conexão com a internet;
- Separa visualmente **Temperos, Especiarias & Aromáticos** dos **Ingredientes Base** em cada receita e sugere automaticamente novos temperos que harmonizam por coocorrência real nas receitas.

---

## 2. Objetivos do Produto

1. **Facilitar a descoberta culinária por combinação:** permitir selecionar múltiplos temperos e ingredientes e ranquear receitas por compatibilidade (`matchScore`).
2. **Apoiar a harmonização de temperos:** calcular e sugerir especiarias/aromáticos que frequentemente acompanham os itens já escolhidos pelo usuário.
3. **Garantir disponibilidade contínua (Offline First):** oferecer pré-carga inicial local (*seed*) e sincronização transparente com persistência em banco Room.
4. **Aplicar engenharia de software Android moderna:** seguir estritamente Clean Architecture, Kotlin Coroutines/Flow, Hilt, Retrofit, Room, Jetpack Compose com Navigation Compose e testes unitários automatizados.

---

## 3. Personas e Fluxos Principais

| Persona | Necessidade | Fluxo no TemperoCerto |
|---|---|---|
| **Cozinheiro(a) do dia a dia** | Tem alho, gengibre e frango em casa e quer saber qual prato fazer e qual outro tempero combina. | Abre a aba **Combinar**, seleciona *Chicken*, *Garlic* e *Ginger*, observa sugestões de harmonização (ex.: *Soy Sauce*, *Coriander*) e abre a receita com maior compatibilidade. |
| **Explorador(a) gastronômico(a)** | Quer pesquisar receitas por nome ou categoria e visualizar claramente quais temperos são usados. | Abre a aba **Receitas**, filtra por categoria ou digita na busca, abre os **Detalhes** e visualiza os cards separados de *Temperos & Aromáticos* e *Ingredientes Base*. |
| **Usuário(a) na cozinha sem Wi-Fi** | Precisa consultar o modo de preparo e seus pratos salvos mesmo sem internet. | O app detecta ausência de rede via `ConnectivityObserver`, exibe o banner de **Modo Offline ativo** e continua respondendo normalmente usando o banco Room local. |

---

## 4. Requisitos Funcionais (RF)

- **RF01 — Listagem e Busca de Receitas:** Exibir receitas armazenadas localmente e permitir busca textual por nome, categoria ou região culinária; quando online, consultar também a API remota e atualizar o cache local.
- **RF02 — Filtro por Categoria:** Permitir filtrar receitas por categoria culinária (ex.: *Todas*, *Chicken*, *Beef*, *Seafood*, *Vegetarian*, *Pasta*, etc.).
- **RF03 — Catálogo de Temperos e Ingredientes:** Disponibilizar catálogo pesquisável classificado por tipo (*Temperos & Especiarias*, *Molhos & Condimentos*, *Proteínas*, *Vegetais & Raízes*, *Grãos & Massas*, *Laticínios*).
- **RF04 — Combinador Multi-Ingrediente/Tempero:** Permitir selecionar e remover múltiplos itens simultaneamente, exibindo receitas ranqueadas por compatibilidade (`matchScore`), itens encontrados, itens selecionados ausentes e temperos extras da receita.
- **RF05 — Sugestão Inteligente de Harmonização de Temperos:** Calcular por coocorrência nas receitas compatíveis quais temperos/especiarias sugeríveis ainda não selecionados mais combinam com a seleção atual.
- **RF06 — Detalhamento Estruturado da Receita:** Exibir foto (com fallback offline), categoria, origem, tags, separação explícita entre **Temperos, Especiarias & Aromáticos** e **Ingredientes Base** (com suas respectivas medidas), além do **Modo de Preparo** e link opcional para vídeo tutorial.
- **RF07 — Meu Livro (Favoritos):** Permitir favoritar e desfavoritar qualquer receita em todas as telas principais e consultá-las em aba dedicada 100% offline.
- **RF08 — Sincronização com Base Online:** Sincronizar catálogo de ingredientes e acervo inicial/sob demanda com a API TheMealDB v1, preservando dados locais do usuário.
- **RF09 — Operação Offline First Transparente:** Garantir que consulta, busca local, combinação de temperos, harmonização, detalhes já cacheados e favoritos funcionem sem rede desde a primeira execução (via *seed* local).
- **RF10 — Indicador de Conectividade e Estados de UI:** Apresentar banner claro quando offline e representar explicitamente estados de **carregamento**, **sucesso**, **ausência de dados** e **erro** com opção de tentar novamente.

---

## 5. Requisitos Não Funcionais (RNF)

- **RNF01 — Arquitetura Clean Architecture:** Separação estrita entre `presentation`, `domain` e `data`, respeitando a regra de dependência (a camada `domain` não possui dependências de frameworks Android, Room ou Retrofit).
- **RNF02 — Injeção de Dependências:** Uso de **Dagger Hilt** (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, módulos dedicados).
- **RNF03 — Programação Assíncrona:** Uso exclusivo de **Kotlin Coroutines**, **Flow** e **StateFlow**, com `Dispatcher` injetado (`@IoDispatcher`, `@DefaultDispatcher`) e zero operações bloqueantes na Main Thread.
- **RNF04 — Interface Moderna e Otimizada:** Construída 100% em **Jetpack Compose** (Material 3) com **Navigation Compose**, estados `@Immutable`, `collectAsStateWithLifecycle()`, `key` estáveis em listas preguiçosas e separação Route/Screen stateless.
- **RNF05 — Persistência Local SSOT:** Uso de **Room** como *Single Source of Truth* para receitas e catálogo de ingredientes.
- **RNF06 — Consumo REST Resiliente:** Uso de **Retrofit + OkHttp + Gson** com timeouts configurados, tratamento explícito de exceções de rede (`IOException`, `HttpException`) e relançamento obrigatório de `CancellationException`.
- **RNF07 — Prevenção de Memory Leaks:** Uso de `@ApplicationContext`, `callbackFlow` com `awaitClose` no monitoramento de rede, e ViewModels livres de referências a `Activity`/`Context` de UI.
- **RNF08 — Confiabilidade e Testes:** Suíte de testes unitários automatizados cobrindo casos de uso, classificadores, mappers, repositório Offline First e ViewModels.

---

## 6. Regras de Negócio (RN)

- **RN01 — Cálculo de Compatibilidade (`matchScore`):** Para $N > 0$ itens selecionados e $M$ itens encontrados na receita (comparação case-insensitive e tolerante a substring culinária, ex.: `"Garlic"` casa com `"minced garlic"`), define-se $\text{matchScore} = M / N$, sendo `isFullMatch = (M == N)`. A ordenação prioriza maior `matchScore`, maior quantidade de itens casados, receitas favoritadas e ordem alfabética.
- **RN02 — Classificação entre Temperos e Ingredientes Base:** O domínio classifica cada ingrediente usando o tipo oficial da API (`Seasoning`, `Spice`, `Sauce`, `Dressing`, etc.) combinado a um classificador semântico de especiarias, ervas, aromáticos e condimentos (`SpiceClassifier`).
- **RN03 — Filtro de Temperos Sugeríveis na Harmonização:** Itens neutros ou básicos universais (como `water`, `boiling water`, `ice`, `salt`, `oil`, `vegetable oil`) não são promovidos como sugestões de harmonização para manter recomendações culinariamente úteis.
- **RN04 — Preservação de Favoritos na Sincronização:** Toda operação de `upsert` vinda da API remota deve consultar previamente os IDs favoritados no Room e preservar `isFavorite = true`.
- **RN05 — Estratégia de Fallback Offline:** Caso a chamada remota falhe por ausência de conectividade ou timeout, o repositório não propaga crash e entrega os resultados diretamente do acervo local do Room.
