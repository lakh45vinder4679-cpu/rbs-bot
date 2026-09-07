package com.lakhvinder.rbsbot.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = ElectricBlueLight,
    onPrimary = Color(0xFF0A1140),
    primaryContainer = DeepIndigo,
    onPrimaryContainer = Color(0xFFE3E8FF),
    secondary = SaffronGold,
    onSecondary = Color(0xFF241A00),
    secondaryContainer = Color(0xFF3A2E05),
    onSecondaryContainer = SaffronLight,
    tertiary = PurpleNeon,
    background = Midnight,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = OutlinesDark,
    error = Danger,
    onError = Color.White
)

private val LightColors = lightColorScheme(
    primary = DeepIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE0FF),
    onPrimaryContainer = Color(0xFF0E194F),
    secondary = Color(0xFFB77E00),
    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF10131A),
    surface = Color.White,
    onSurface = Color(0xFF10131A),
    surfaceVariant = Color(0xFFE8EBF4),
    onSurfaceVariant = Color(0xFF4A5265)
)

@Composable
fun RbsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = RbsTypography,
        content = content
    )
}
