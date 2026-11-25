package br.edu.orlandovandres.temperocerto.presentation.combiner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.orlandovandres.temperocerto.domain.model.CombinationMatch
import br.edu.orlandovandres.temperocerto.presentation.components.EmptyStateView
import br.edu.orlandovandres.temperocerto.presentation.components.ErrorStateView
import kotlin.math.roundToInt

@Composable
fun CombinerRoute(
    onRecipeClick: (String) -> Unit,
    viewModel: CombinerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CombinerScreen(
        state = state,
        onCatalogQueryChange = viewModel::onCatalogQueryChange,
        onTabSelected = viewModel::onTabSelected,
        onAddItem = viewModel::onAddItem,
        onRemoveItem = viewModel::onRemoveItem,
        onClearSelection = viewModel::onClearSelection,
        onRetrySearch = viewModel::onRetrySearch,
        onToggleFavorite = viewModel::onToggleFavorite,
        onRecipeClick = onRecipeClick
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CombinerScreen(
    state: CombinerUiState,
    onCatalogQueryChange: (String) -> Unit,
    onTabSelected: (CatalogFilterTab) -> Unit,
    onAddItem: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearSelection: () -> Unit,
    onRetrySearch: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Combinador de Temperos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Escolha temperos e ingredientes para descobrir harmonizações e receitas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "selected_tray") {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selecionados (${state.selectedItems.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (state.selectedItems.isNotEmpty()) {
                            TextButton(onClick = onClearSelection) {
                                Text("Limpar tudo")
                            }
                        }
                    }

                    if (state.selectedItems.isEmpty()) {
                        Text(
                            text = "Toque nos temperos ou ingredientes abaixo para iniciar uma combinação.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            state.selectedItems.forEach { item ->
                                InputChip(
                                    selected = true,
                                    onClick = { onRemoveItem(item) },
                                    label = { Text(item) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Remover $item",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.suggestedPairings.isNotEmpty()) {
            item(key = "pairings_section") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (state.selectedItems.isEmpty()) {
                                "Temperos populares para começar"
                            } else {
                                "Temperos que harmonizam"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.suggestedPairings,
                            key = { it.spiceName }
                        ) { suggestion ->
                            SuggestionChip(
                                onClick = { onAddItem(suggestion.spiceName) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text("${suggestion.spiceName} (${suggestion.recipeCount})")
                                }
                            )
                        }
                    }
                }
            }
        }

        item(key = "catalog_selector") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.catalogQuery,
                    onValueChange = onCatalogQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Filtrar catálogo (ex: Garlic, Chicken, Paprika)...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (state.catalogQuery.isNotEmpty()) {
                            IconButton(onClick = { onCatalogQueryChange("") }) {
                                Icon(imageVector = Icons.Outlined.Close, contentDescription = "Limpar")
                            }
                        }
                    }
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        items = CatalogFilterTab.entries,
                        key = { it.name }
                    ) { tab ->
                        FilterChip(
                            selected = tab == state.selectedTab,
                            onClick = { onTabSelected(tab) },
                            label = { Text(tab.label) }
                        )
                    }
                }

                if (state.isLoadingCatalog) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Carregando catálogo...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (state.availableCatalog.isEmpty()) {
                    Text(
                        text = "Nenhum item disponível para esse filtro no catálogo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(
                            items = state.availableCatalog,
                            key = { it.id }
                        ) { catalogItem ->
                            AssistChip(
                                onClick = { onAddItem(catalogItem.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(catalogItem.name) }
                            )
                        }
                    }
                }
            }
        }

        item(key = "results_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Receitas Compatíveis (${state.matches.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                if (state.isSearchingMatches) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp
                    )
                }
            }
        }

        when {
            state.selectedItems.isEmpty() -> {
                item(key = "empty_prompt") {
                    EmptyStateView(
                        icon = Icons.Outlined.Spa,
                        title = "Monte sua combinação",
                        subtitle = "Selecione um ou mais temperos e ingredientes acima para ver receitas ranqueadas por afinidade.",
                        modifier = Modifier.height(220.dp)
                    )
                }
            }

            state.errorMessage != null && state.matches.isEmpty() -> {
                item(key = "error_matches") {
                    ErrorStateView(
                        message = state.errorMessage,
                        onRetry = onRetrySearch,
                        modifier = Modifier.height(220.dp)
                    )
                }
            }

            !state.isSearchingMatches && state.matches.isEmpty() -> {
                item(key = "no_matches") {
                    EmptyStateView(
                        icon = Icons.Outlined.RestaurantMenu,
                        title = "Nenhuma receita encontrada",
                        subtitle = "Experimente remover um item ou escolher outra combinação de temperos.",
                        actionLabel = "Limpar seleção",
                        onAction = onClearSelection,
                        modifier = Modifier.height(220.dp)
                    )
                }
            }

            else -> {
                items(
                    items = state.matches,
                    key = { it.recipe.id }
                ) { match ->
                    CombinationMatchCard(
                        match = match,
                        onClick = { onRecipeClick(match.recipe.id) },
                        onToggleFavorite = { onToggleFavorite(match.recipe.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CombinationMatchCard(
    match: CombinationMatch,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val percent = (match.matchScore * 100.0).roundToInt()

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (match.isFullMatch) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    }
                ) {
                    Text(
                        text = "$percent% compatível (${match.matchedItems.size}/${match.matchedItems.size + match.missingSelectedItems.size})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (match.isFullMatch) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (match.recipe.isFavorite) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                        contentDescription = "Favoritar",
                        tint = if (match.recipe.isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Text(
                text = match.recipe.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "${match.recipe.category} • ${match.recipe.area}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                match.matchedItems.forEach { matched ->
                    AssistChip(
                        onClick = onClick,
                        label = { Text("✓ $matched") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        )
                    )
                }
                match.missingSelectedItems.forEach { missing ->
                    AssistChip(
                        onClick = onClick,
                        label = { Text("Sem $missing") }
                    )
                }
            }

            if (match.extraSpicesInRecipe.isNotEmpty()) {
                Text(
                    text = "Outros temperos na receita: ${match.extraSpicesInRecipe.take(5).joinToString(", ")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
