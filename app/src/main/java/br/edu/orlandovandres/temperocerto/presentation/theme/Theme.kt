package br.edu.orlandovandres.temperocerto.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PaprikaPrimary,
    onPrimary = PaprikaOnPrimary,
    primaryContainer = PaprikaContainer,
    onPrimaryContainer = PaprikaOnContainer,
    secondary = SageSecondary,
    onSecondary = SageOnSecondary,
    secondaryContainer = SageContainer,
    onSecondaryContainer = SageOnContainer,
    tertiary = SaffronTertiary,
    onTertiary = SaffronOnTertiary,
    tertiaryContainer = SaffronContainer,
    onTertiaryContainer = SaffronOnContainer,
    background = WarmBackground,
    onBackground = WarmOnSurface,
    surface = WarmSurface,
    onSurface = WarmOnSurface,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = WarmOnSurfaceVariant,
    outline = WarmOutline
)

private val ColorSchemeDarkPrimary = androidx.compose.ui.graphics.Color(0xFFFFB4A1)

private val DarkColorScheme = darkColorScheme(
    primary = ColorSchemeDarkPrimary,
    onPrimary = PaprikaOnContainer,
    primaryContainer = PaprikaPrimary,
    onPrimaryContainer = PaprikaContainer,
    secondary = SageContainer,
    onSecondary = SageOnContainer,
    secondaryContainer = SageSecondary,
    onSecondaryContainer = SageContainer,
    tertiary = SaffronContainer,
    onTertiary = SaffronOnContainer,
    tertiaryContainer = SaffronTertiary,
    onTertiaryContainer = SaffronContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

@Composable
fun TemperoCertoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TemperoTypography,
        content = content
    )
}
