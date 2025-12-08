package com.rcsi.wellby.chatTab.views
// View that decides whether to display the view for coaches or students based on their assigned role
// this role is established when they create an account in the app and it is stored in Firebase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.rcsi.wellby.signinSystem.AuthManager

@Composable
fun MainMessageView(userManager: AuthManager, navController: NavController) {

    val currentUser = userManager.currentUser.collectAsState().value

    Column {
        if (currentUser?.student == false) {
            CoachView(userManager = userManager, navController = navController)
        } else {
            StudentView(userManager = userManager, navController = navController)
        }
    }

}