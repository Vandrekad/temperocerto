package br.edu.orlandovandres.temperocerto.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.edu.orlandovandres.temperocerto.domain.repository.ConnectivityObserver
import br.edu.orlandovandres.temperocerto.presentation.combiner.CombinerRoute
import br.edu.orlandovandres.temperocerto.presentation.components.OfflineBanner
import br.edu.orlandovandres.temperocerto.presentation.detail.RecipeDetailRoute
import br.edu.orlandovandres.temperocerto.presentation.favorites.FavoritesRoute
import br.edu.orlandovandres.temperocerto.presentation.recipes.RecipesRoute

private data class TopLevelDestination(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Screen.Recipes, "Receitas", Icons.AutoMirrored.Outlined.MenuBook),
    TopLevelDestination(Screen.Combiner, "Combinar", Icons.Outlined.AutoAwesome),
    TopLevelDestination(Screen.Favorites, "Meu Livro", Icons.Outlined.FavoriteBorder)
)

@Composable
fun TemperoCertoAppRoot(
    connectivityObserver: ConnectivityObserver
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isOnline by connectivityObserver.isOnline.collectAsStateWithLifecycle(
        initialValue = connectivityObserver.currentIsOnline()
    )

    val showBottomBar = topLevelDestinations.any { it.screen.route == currentRoute }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        val selected = currentRoute == destination.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .then(
                    if (showBottomBar) {
                        Modifier.statusBarsPadding()
                    } else {
                        Modifier
                    }
                )
        ) {
            if (showBottomBar) {
                OfflineBanner(isOnline = isOnline)
            }

            NavHost(
                navController = navController,
                startDestination = Screen.Recipes.route,
                modifier = Modifier.weight(1f)
            ) {
                composable(route = Screen.Recipes.route) {
                    RecipesRoute(
                        onRecipeClick = { recipeId ->
                            navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                        }
                    )
                }

                composable(route = Screen.Combiner.route) {
                    CombinerRoute(
                        onRecipeClick = { recipeId ->
                            navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                        }
                    )
                }

                composable(route = Screen.Favorites.route) {
                    FavoritesRoute(
                        onRecipeClick = { recipeId ->
                            navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                        },
                        onExploreClick = {
                            navController.navigate(Screen.Recipes.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable(
                    route = Screen.RecipeDetail.route,
                    arguments = listOf(
                        navArgument("recipeId") { type = NavType.StringType }
                    )
                ) {
                    RecipeDetailRoute(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
