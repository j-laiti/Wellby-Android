package com.rcsi.wellby.chatTab.aiChat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.rcsi.wellby.chatTab.views.getScreenWidthFraction
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import com.rcsi.wellby.R
import com.rcsi.wellby.signinSystem.AuthManager

@Composable
fun AiChatScreen(aiMessagesManager: AiMessagesManager, userManager: AuthManager) {
    val messages by aiMessagesManager.aiMessages.collectAsState()
    val isLoading by aiMessagesManager.isLoading.collectAsState()
    var userMessage by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.chaticon), // Replace with your image name
                contentDescription = "AI Chat Icon",
                modifier = Modifier
                    .size(50.dp)
            )
            Text(
                text = "Wellby AI",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(messages) { message ->
                MessageBlock(message = message)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp) // Adjust size as needed
                        )
                    }
                }
            }
        }

        LaunchedEffect(messages) {
            if (messages.isNotEmpty() || isLoading) {
                listState.animateScrollToItem(messages.size) // Scroll to the last item
            }
        }

        // Message Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                modifier = Modifier.weight(1f),
                label = { Text("Type a message") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (userMessage.trim().isNotEmpty()) {
                        aiMessagesManager.sendMessage(userMessage.trim())
                        userMessage = "" // Clear input field
                    }
                    userManager.clickedOn("send Wellby AI a message")
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message"
                )
            }
        }
    }
}

@Composable
fun MessageBlock(message: AiMessage) {
    val maxWidth = getScreenWidthFraction(0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.received) Arrangement.Start else Arrangement.End
    ) {
        Text(
            text = message.text,
            modifier = Modifier
                .background(
                    if (message.received) MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .widthIn(max = maxWidth),
            style = TextStyle(
                color = Color.White,
                fontSize = androidx.compose.material.MaterialTheme.typography.body1.fontSize,
                fontWeight = FontWeight.Normal
            )
        )
    }
}

