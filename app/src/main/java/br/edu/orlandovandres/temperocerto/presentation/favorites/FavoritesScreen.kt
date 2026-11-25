package br.edu.orlandovandres.temperocerto.presentation.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.orlandovandres.temperocerto.presentation.components.EmptyStateView
import br.edu.orlandovandres.temperocerto.presentation.components.LoadingStateView
import br.edu.orlandovandres.temperocerto.presentation.components.RecipeCard

@Composable
fun FavoritesRoute(
    onRecipeClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritesScreen(
        state = state,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onToggleFavorite = viewModel::onToggleFavorite,
        onRecipeClick = onRecipeClick,
        onExploreClick = onExploreClick
    )
}

@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onSearchQueryChange: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRecipeClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Meu Livro",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Receitas salvas para consulta rápida 100% offline",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            placeholder = { Text("Filtrar receitas salvas...") },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Limpar")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.isLoading -> {
                LoadingStateView(message = "Abrindo seu livro de receitas...")
            }

            state.favorites.isEmpty() -> {
                EmptyStateView(
                    icon = Icons.Outlined.BookmarkBorder,
                    title = if (state.searchQuery.isEmpty()) {
                        "Seu livro está vazio"
                    } else {
                        "Nenhuma receita salva encontrada"
                    },
                    subtitle = if (state.searchQuery.isEmpty()) {
                        "Toque no coração em qualquer receita para mantê-la salva no seu livro offline."
                    } else {
                        "Nenhuma receita favorita corresponde à busca atual."
                    },
                    actionLabel = if (state.searchQuery.isEmpty()) "Explorar receitas" else "Limpar filtro",
                    onAction = {
                        if (state.searchQuery.isEmpty()) {
                            onExploreClick()
                        } else {
                            onSearchQueryChange("")
                        }
                    }
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(
                        items = state.favorites,
                        key = { it.id }
                    ) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe.id) },
                            onToggleFavorite = { onToggleFavorite(recipe.id) }
                        )
                    }
                }
            }
        }
    }
}
