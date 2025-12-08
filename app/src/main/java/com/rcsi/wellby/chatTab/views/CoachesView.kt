package com.rcsi.wellby.chatTab.views
// There are two roles in this app either a student or a coach. Coaches have a different view
// that allows them to message any student assigned to them. This view includes a list of
// conversations between this coach and their students and the ability to start new messages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rcsi.wellby.chatTab.ChatManager
import com.rcsi.wellby.chatTab.ChatManagerFactory
import com.rcsi.wellby.chatTab.RecentMessage
import com.rcsi.wellby.signinSystem.AuthManager
import com.rcsi.wellby.signinSystem.User
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import com.rcsi.wellby.chatTab.getTimeAgo

@Composable
fun CoachView(userManager: AuthManager, navController: NavController) {

    val chatManager: ChatManager = viewModel(
        factory = ChatManagerFactory(userManager)
    )

    val currentUser = userManager.currentUser.collectAsState().value
    val isEditing = remember { mutableStateOf(false) }
    val statusText = remember { mutableStateOf("") }

    var showUserList by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = chatManager) {
        chatManager.fetchRecentMessages()
    }

    Box (
       modifier = Modifier.fillMaxSize()
    ) {
    Column {
        if (showUserList) {
            UserListView(userManager = userManager, navController = navController) {
                showUserList = false
            }
        }

        MessageHeader(
            currentUser = currentUser,
            isEditing = isEditing,
            statusText = statusText,
            onStatusChange = { newStatus ->
                if (currentUser != null && !currentUser.student) {
                    userManager.updateCoachStatus(newStatus)
                }
            }
        )

        // List recent messages
        MessageList(
            userManager = userManager,
            chatManager = chatManager,
            navController = navController
        )

        Spacer(modifier = Modifier.weight(1f))
    }
        Column (
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) {
            // Start a new conversation
            NewMessageButton {
                userManager.fetchAssignedStudents()
                showUserList = true
            }
        }
    }
}

@Composable
fun MessageHeader(
    currentUser: User?,
    isEditing: MutableState<Boolean>,
    statusText: MutableState<String>,
    onStatusChange: (String) -> Unit
) {
    if (currentUser != null) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "Hi ${currentUser.username}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isEditing.value) {
                    TextField(
                        value = statusText.value,
                        onValueChange = { statusText.value = it },
                        label = { Text("Enter new status") },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text("Current status: ${currentUser.status}", modifier = Modifier.weight(1f))
                }
                IconButton(onClick = {
                    if (isEditing.value && statusText.value != currentUser.status) {
                        onStatusChange(statusText.value)
                    }
                    isEditing.value = !isEditing.value
                }) {
                    Icon(
                        imageVector = if (isEditing.value) Icons.Filled.Check else Icons.Filled.Edit,
                        contentDescription = "Edit Status",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}



@Composable
fun MessageList(userManager: AuthManager, chatManager: ChatManager, navController: NavController) {
    val recentMessages by chatManager.recentMessages.collectAsState()

    LazyColumn {
        items(recentMessages.size) { index ->
            val message = recentMessages[index]
            val selectedUser = remember { mutableStateOf<User?>(null) }
            val currentUserId = userManager.currentUser.collectAsState().value?.id
            var chatUserId = ""

            MessageRow(message = message, onClick = {
                if (currentUserId == message.currentID) {
                    chatUserId = message.chatUserID
                } else {
                    chatUserId = message.currentID
                }

                if (!message.viewed) {
                    chatManager.viewStatusTrue()
                }
                //fetch user by their ID using the message ID
                userManager.fetchUserById(chatUserId) { user ->
                    if (user != null) {
                        selectedUser.value = user
                    }

                }
            })

            if (index < recentMessages.size - 1) { // Add divider but not after the last item
                Divider(color = MaterialTheme.colorScheme.onSecondary, thickness = 1.dp, modifier = Modifier.padding(horizontal = 30.dp, vertical = 8.dp))
            }

            LaunchedEffect(selectedUser.value) {
                selectedUser.value?.let {
                    userManager.chatUser.value = it  // Set the fetched user as the chatUser
                    navController.navigate("chatScreen")
                }
            }
        }
    }
}


@Composable
fun MessageRow(message: RecentMessage, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .clickable { onClick() }
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            if (!message.viewed) {
                // Display a blue circle if the message hasn't been viewed
                Canvas(modifier = Modifier.size(10.dp), onDraw = {
                    drawCircle(
                        color = Color.Blue,
                        center = Offset(x = this.size.width / 2, y = this.size.height / 2),
                        radius = size.minDimension / 2
                    )
                })
                Spacer(modifier = Modifier.width(5.dp)) // Space between the circle and the text
            } else {
                Spacer(modifier = Modifier.width(10.dp)) // Space to align texts when no circle is shown
            }

            Icon(Icons.Filled.AccountCircle,
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 10.dp).size(35.dp),
                tint = MaterialTheme.colorScheme.primary)

            Column (modifier = Modifier.weight(1f)) {
                Text(message.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    message.message,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.DarkGray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = getTimeAgo(message.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
            )
        }
        //Spacer(modifier = Modifier.weight(1f))

    }
}

@Composable
fun NewMessageButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("New Message", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}


@Composable
fun UserListView(userManager: AuthManager, navController: NavController, onClose: () -> Unit) {
    val userList = userManager.userList.collectAsState().value

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Select a User") },
        text = {
            Column {
                LazyColumn {
                    items(userList.size) { index ->

                        val user = userList[index]

                        UserItem(user = user, onClick = {
                            userManager.chatUser.value = user
                            navController.navigate("chatScreen")
                        })
                    }
                }

                Button(onClick = {
                    navController.navigate("messageAll")
                    onClose()
                },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("Message All Students")
                }
            }
        },
        confirmButton = {
            Button(onClick = onClose) {
                Text("Close")
            }
        }
    )
}

@Composable
fun UserItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = user.username, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.weight(1f))
    }
}
