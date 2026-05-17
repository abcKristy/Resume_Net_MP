package com.example.resume_net.ui.theme

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
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = OnSurfaceDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)


@Composable
fun Resume_netTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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


val androidx.compose.material3.ColorScheme.tagScoreHigh: Color
    @Composable
    get() = TagScoreHigh

val androidx.compose.material3.ColorScheme.tagScoreMedium: Color
    @Composable
    get() = TagScoreMedium

val androidx.compose.material3.ColorScheme.tagScoreLow: Color
    @Composable
    get() = TagScoreLow

val androidx.compose.material3.ColorScheme.tagHigh: Color
    @Composable
    get() = TagHighColor

val androidx.compose.material3.ColorScheme.tagMedium: Color
    @Composable
    get() = TagMediumColor

val androidx.compose.material3.ColorScheme.tagLow: Color
    @Composable
    get() = TagLowColor

val androidx.compose.material3.ColorScheme.scoreHighGradientStart: Color
    @Composable
    get() = ScoreHighGradientStart

val androidx.compose.material3.ColorScheme.scoreHighGradientEnd: Color
    @Composable
    get() = ScoreHighGradientEnd

val androidx.compose.material3.ColorScheme.scoreMediumGradientStart: Color
    @Composable
    get() = ScoreMediumGradientStart

val androidx.compose.material3.ColorScheme.scoreMediumGradientEnd: Color
    @Composable
    get() = ScoreMediumGradientEnd

val androidx.compose.material3.ColorScheme.scoreLowGradientStart: Color
    @Composable
    get() = ScoreLowGradientStart

val androidx.compose.material3.ColorScheme.scoreLowGradientEnd: Color
    @Composable
    get() = ScoreLowGradientEnd

val androidx.compose.material3.ColorScheme.assistantAvatarStart: Color
    @Composable
    get() = AssistantAvatarGradientStart

val androidx.compose.material3.ColorScheme.assistantAvatarEnd: Color
    @Composable
    get() = AssistantAvatarGradientEnd

val androidx.compose.material3.ColorScheme.inputFieldBackground: Color
    @Composable
    get() = InputFieldBackground