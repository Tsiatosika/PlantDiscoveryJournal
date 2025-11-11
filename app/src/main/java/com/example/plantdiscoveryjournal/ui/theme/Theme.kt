package com.example.plantdiscoveryjournal.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.plantdiscoveryjournal.ui.theme.Background
import com.example.plantdiscoveryjournal.ui.theme.OnBackground
import com.example.plantdiscoveryjournal.ui.theme.OnError
import com.example.plantdiscoveryjournal.ui.theme.OnPrimary
import com.example.plantdiscoveryjournal.ui.theme.OnSecondary
import com.example.plantdiscoveryjournal.ui.theme.OnSurface
import com.example.plantdiscoveryjournal.ui.theme.Primary
import com.example.plantdiscoveryjournal.ui.theme.PrimaryLight
import com.example.plantdiscoveryjournal.ui.theme.Secondary
import com.example.plantdiscoveryjournal.ui.theme.SurfaceVariant
import com.example.plantdiscoveryjournal.ui.theme.Tertiary

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = OnPrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SurfaceVariant,
    onSecondaryContainer = OnSurface,
    tertiary = Tertiary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    error = Error,
    onError = OnError
)

@Composable
fun PlantDiscoveryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}