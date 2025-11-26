package edu.augusta.scc.cinerateui.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Simple light color scheme – you can customize later if you want
private val LightColors = lightColorScheme()

@Composable
fun CineRateUITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}

