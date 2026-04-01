package com.sandeep.personalfinancecompanion.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAccent,
    onPrimary = PrimaryDark,
    primaryContainer = PrimarySurface,
    onPrimaryContainer = PrimaryAccentLight,
    secondary = PrimaryAccentLight,
    onSecondary = PrimaryDark,
    secondaryContainer = PrimarySurface,
    onSecondaryContainer = TextWhite,
    tertiary = ChartColor2,
    onTertiary = Color.White,
    background = PrimaryDark,
    onBackground = TextWhite,
    surface = PrimaryMedium,
    onSurface = TextWhite,
    surfaceVariant = PrimarySurface,
    onSurfaceVariant = TextGrey,
    outline = TextLightGrey,
    error = ExpenseRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00352E),
    secondary = Color(0xFF4DB6AC),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00352E),
    tertiary = ChartColor2,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFE0E3E7),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F),
    error = ExpenseRed,
    onError = Color.White
)

@Composable
fun PersonalFinanceCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to keep our custom palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}