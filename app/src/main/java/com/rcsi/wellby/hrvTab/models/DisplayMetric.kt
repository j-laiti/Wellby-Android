package com.rcsi.wellby.hrvTab.models
// model used in the display of the metrics highlighted in the main screen of this tab
import androidx.compose.ui.graphics.vector.ImageVector

data class DisplayMetric(
    val name: String,
    val icon: ImageVector,
    val value: String
)
