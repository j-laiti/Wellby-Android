package com.rcsi.wellby.toolkitTab.ColorPicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.rcsi.wellby.toolkitTab.ColorPicker.DataStoreManager.selectedPrimaryColorFlow
import com.rcsi.wellby.toolkitTab.ColorPicker.DataStoreManager.selectedSecondaryColorFlow

@Composable
fun ProvideAppColors(content: @Composable (Color, Color) -> Unit) {
    val context = LocalContext.current
    val primaryColorString by context.selectedPrimaryColorFlow.collectAsState(initial = "#FFFFFF")
    val secondaryColorString by context.selectedSecondaryColorFlow.collectAsState(initial = "#000000") // Default to black for secondary

    val primaryColor = try {
        Color(android.graphics.Color.parseColor(primaryColorString))
    } catch (e: IllegalArgumentException) {
        Color.Blue // Default if parsing fails
    }

    val secondaryColor = try {
        Color(android.graphics.Color.parseColor(secondaryColorString))
    } catch (e: IllegalArgumentException) {
        Color.Gray // Default if parsing fails
    }

    content(primaryColor, secondaryColor)
}

