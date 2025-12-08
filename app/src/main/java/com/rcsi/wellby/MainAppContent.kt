package com.rcsi.wellby

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.rcsi.wellby.hrvTab.helpers.PermissionHandler
import com.rcsi.wellby.signinSystem.AuthManager
import com.rcsi.wellby.toolkitTab.toDoList.ToDoRepository

@Composable
fun MainAppContent(authManager: AuthManager, toDoRepository: ToDoRepository, permissionHandler: PermissionHandler) {
    val navController = rememberNavController()

    Scaffold (
        //defines the bottom bar view and how navigation should be handled
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) {
            // this is telling the main screen which composable to show based on the navController
            BottomNavGraph(navController, authManager, toDoRepository, permissionHandler)
        }
    }
}