package com.rcsi.wellby.ui.theme
// unused gradient background testing with different phone themes

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun GradientBackground(content: @Composable () -> Unit) {
    // Determine if you're in light or dark theme, replace 'isSystemInDarkTheme()' 
    // with your own theme check if you have custom logic for theme determination.
    val colors = if (!isSystemInDarkTheme()) {
        listOf(Color.Blue.copy(alpha = 0.7f), Color.White) // Light theme gradient
    } else {
        listOf(Color.Transparent, Color.Transparent) // Dark theme solid color
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors))
    ) {
        content()
    }
}
