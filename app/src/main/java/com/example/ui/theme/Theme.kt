package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.stringsFor

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyContainer,
    onPrimaryContainer = OnNavyContainer,
    secondary = AmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = OnAmberContainer,
    tertiary = SteelTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SteelContainer,
    onTertiaryContainer = OnSteelContainer,
    background = ConcreteBackground,
    onBackground = TextPrimaryLight,
    surface = CardSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    error = StatusDanger,
    onError = Color.White,
    errorContainer = StatusDangerBg
)

private val DarkColorScheme = darkColorScheme(
    primary = AmberBright,
    onPrimary = Color(0xFF1C0C00),
    primaryContainer = NavyElevated,
    onPrimaryContainer = Color(0xFFE5EDF7),
    secondary = AmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A2005),
    onSecondaryContainer = AmberContainer,
    tertiary = Color(0xFF60A5FA),
    onTertiary = Color(0xFF061B36),
    tertiaryContainer = Color(0xFF133359),
    onTertiaryContainer = SteelContainer,
    background = BlueprintBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = CardSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF5C1515)
)

val BtpShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BtpManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    language: AppLanguage = AppLanguage.AR,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val appStrings = stringsFor(language)

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalAppStrings provides appStrings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BtpTypography,
            shapes = BtpShapes,
            content = content
        )
    }
}
