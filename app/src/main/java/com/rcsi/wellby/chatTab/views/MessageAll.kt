package com.rcsi.wellby.chatTab.views
// This screen is for coaches only and allows them to send a message that will be broadcasted to
// all of their assigned students

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rcsi.wellby.chatTab.ChatManager
import com.rcsi.wellby.chatTab.ChatManagerFactory
import com.rcsi.wellby.signinSystem.AuthManager

@Composable
fun MessageAllScreen(userManager: AuthManager, navController: NavController) {

    val chatManager: ChatManager = viewModel(
        factory = ChatManagerFactory(userManager)
    )

    var message by remember { mutableStateOf("") }
    val users = userManager.userList.collectAsState().value

    Column {
        Text(
            "Send a message to all students:",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        TextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )
        Button(
            onClick = {
                chatManager.messageAllStudents(message)
                message = "" // Clear the message after sending
                navController.navigateUp() // Go back to the previous screen
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Send")
        }
    }
}
