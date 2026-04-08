package io.taiga.client.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = TaigaPrimary,
    onPrimary = TaigaOnPrimary,
    secondary = TaigaSecondary,
    onSecondary = Color.White,
    tertiary = TaigaTertiary,
    onTertiary = Color.White,
    background = TaigaBackground,
    onBackground = Color(0xFF111827),
    surface = TaigaSurface,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE7E2D7),
    onSurfaceVariant = Color(0xFF475569),
    error = TaigaError,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7DD3FC),
    onPrimary = Color(0xFF082F49),
    secondary = Color(0xFFFBBF24),
    tertiary = Color(0xFF5EEAD4),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFF87171),
)

@Composable
fun TaigaClientTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
