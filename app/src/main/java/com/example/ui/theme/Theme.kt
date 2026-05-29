package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = NeonMagenta,      // #D1E4FF (Sleek primary light blue)
    secondary = NeonCyan,     // #4FD8EB (Sleek light cyan)
    tertiary = GlowPurple,     // #D0BCFF (Sleek lavender)
    background = DeepMidnight,  // #1A1C1E
    surface = DarkCard,        // #2E3033
    onPrimary = Color(0xFF001D36), // Deep dark brand blue for superb contrast on light-blue background
    onSecondary = Color(0xFF001D36),
    onTertiary = Color(0xFF381E72), // Dark purple for contrast on lavender
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = NeonCyan,
    tertiary = NeonMagenta,
    background = TextPrimary,
    surface = PurpleGrey80,
    onPrimary = TextPrimary,
    onSecondary = DeepMidnight,
    onTertiary = TextPrimary,
    onBackground = DeepMidnight,
    onSurface = DeepMidnight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default to deliver the spectacular glowing UI requested by user
    dynamicColor: Boolean = false, // Use our handcrafted premium neon theme instead of dynamic system colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> CyberColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
