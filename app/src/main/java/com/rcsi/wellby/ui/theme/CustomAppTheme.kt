package com.rcsi.wellby.ui.theme
// custom app theme based on the color selected by the user in settings (default is blue)
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.rcsi.wellby.toolkitTab.ColorPicker.DataStoreManager.selectedPrimaryColorFlow
import com.rcsi.wellby.toolkitTab.ColorPicker.DataStoreManager.selectedSecondaryColorFlow

@Composable
fun CustomAppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current

    // Extracting colors from DataStore
    val primaryColorString by context.selectedPrimaryColorFlow.collectAsState(initial = null)
    val secondaryColorString by context.selectedSecondaryColorFlow.collectAsState(initial = null)

    // Convert string color to Color object, with fallback to default colors
    val primaryColor = primaryColorString?.let { Color(android.graphics.Color.parseColor(it)) }
    val secondaryColor = secondaryColorString?.let { Color(android.graphics.Color.parseColor(it)) }

    // Using custom function for color scheme which considers API level
    val customColorScheme = customDynamicLightColorScheme(primaryColor, secondaryColor)

    MaterialTheme(
        colorScheme = customColorScheme,
        content = content
    )
}

@Composable
fun customDynamicLightColorScheme(
    primaryColor: Color?,
    secondaryColor: Color?
): ColorScheme {
    val context = LocalContext.current
    val defaultScheme = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        dynamicLightColorScheme(context)
    } else {
        lightColorScheme(
            // Default colors for older API levels, you can customize these values
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6)
        )
    }

    // Override the default/dynamic scheme with custom colors if they exist
    return defaultScheme.copy(
        primary = primaryColor ?: defaultScheme.primary,
        secondary = secondaryColor ?: defaultScheme.secondary
    )
}
