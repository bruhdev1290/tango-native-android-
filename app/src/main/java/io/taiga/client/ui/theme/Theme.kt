package io.taiga.client.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.taiga.client.data.preferences.AppearanceMode

val accentPresets: List<Color> = listOf(
    Color(0xFF145A6C), // Taiga teal (default)
    Color(0xFFD97706), // Amber
    Color(0xFF7C3AED), // Violet
    Color(0xFFDC2626), // Red
    Color(0xFF16A34A), // Green
    Color(0xFFDB2777), // Pink
    Color(0xFF0284C7), // Sky blue
    Color(0xFF92400E), // Brown
)

private fun lightColors(primary: Color) = lightColorScheme(
    primary = primary,
    onPrimary = Color(0xFFF8FAFC),
    secondary = primary.copy(red = primary.red * 0.9f, green = primary.green * 0.85f, blue = primary.blue * 1.05f),
    onSecondary = Color.White,
    tertiary = primary.copy(red = primary.red * 0.8f, green = primary.green * 1.1f, blue = primary.blue * 0.9f),
    onTertiary = Color.White,
    background = TaigaBackground,
    onBackground = Color(0xFF111827),
    surface = TaigaSurface,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE7E2D7),
    onSurfaceVariant = Color(0xFF475569),
    error = TaigaError,
)

private fun darkColors(primary: Color) = darkColorScheme(
    primary = primary,
    onPrimary = Color(0xFF082F49),
    secondary = primary.copy(red = primary.red * 1.1f, green = primary.green * 0.95f, blue = primary.blue * 0.9f),
    tertiary = primary.copy(red = primary.red * 0.9f, green = primary.green * 1.1f, blue = primary.blue * 1.05f),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFF87171),
)

@Composable
fun TaigaClientTheme(
    appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    accentColorIndex: Int = 0,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (appearanceMode) {
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
    }
    val accentColor = accentPresets.getOrElse(accentColorIndex) { accentPresets[0] }
    val colorScheme = if (darkTheme) darkColors(accentColor) else lightColors(accentColor)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
