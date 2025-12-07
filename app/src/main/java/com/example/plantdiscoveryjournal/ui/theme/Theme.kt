package com.example.plantdiscoveryjournal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette claire
private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = BackgroundWhite,
    background = BackgroundWhite,
    surface = BackgroundGray,
    onBackground = TextBlack,
    onSurface = TextBlack
)

// Palette sombre (noir / gris)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.Black,
    background = Color(0xFF101010),
    surface = Color(0xFF181818),
    onBackground = Color(0xFFECECEC),
    onSurface = Color(0xFFECECEC)
)

@Composable
fun PlantDiscoveryJournalTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
