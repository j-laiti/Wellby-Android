package com.rcsi.wellby
// tab names and icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector

enum class TabOptions(val route: String, val icon: ImageVector, val label: String) {
    Home("home", Icons.Filled.Home, "Home"),
    Resources("resources", Icons.Filled.Menu, "Resources"),
    Biofeedback("biofeedback", Icons.Filled.Favorite, "Biofeedback"),
    Messages("messages", Icons.Filled.ChatBubble, "Messages")
}