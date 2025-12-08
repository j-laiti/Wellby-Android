package com.rcsi.wellby.chatTab.views
// Chat screen code which includes messages between student and coach, link detection in messages
// and options to flag content

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rcsi.wellby.chatTab.ChatManager
import com.rcsi.wellby.chatTab.ChatManagerFactory
import com.rcsi.wellby.chatTab.Message
import com.rcsi.wellby.signinSystem.AuthManager
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.core.content.ContextCompat.startActivity
import com.rcsi.wellby.R


@Composable
fun ChatScreen(userManager: AuthManager) {
    val chatManager: ChatManager = viewModel(
        factory = ChatManagerFactory(userManager)
    )

    LaunchedEffect(key1 = chatManager) {
        chatManager.fetchMessages()
    }

    val messages by chatManager.messages.collectAsState()
    val currentUser = userManager.currentUser.collectAsState().value
    val newMessageText = remember { mutableStateOf("") }
    val chatUsername = userManager.chatUser.value?.username ?: "Unknown"

    // State for showing the dropdown menu
    val showMenu = remember { mutableStateOf(false) }

    // States for showing alerts
    val showAlert = remember { mutableStateOf(false) }
    val alertTitle = remember { mutableStateOf("") }
    val alertMessage = remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val messagesSize = messages.size

    LaunchedEffect(key1 = chatManager, key2 = messagesSize) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(index = messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {

        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 8.dp)
        ) {
            Spacer(modifier = Modifier.weight(1.5f))
            Image(
                painter = painterResource(id = R.drawable.coach), // Replace with your image name
                contentDescription = "AI Chat Icon",
                modifier = Modifier
                    .size(50.dp)
            )
            Text (
                text = chatUsername,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(10.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showMenu.value = true }) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More Options")
            }
            DropdownMenu(
                expanded = showMenu.value,
                onDismissRequest = { showMenu.value = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Flag as Objectionable") },
                    onClick = {
                        chatManager.reportObjectionableContent()
                        alertTitle.value = "Flagged"
                        alertMessage.value = "The conversation has been flagged as objectionable and will be reviewed."
                        showAlert.value = true
                        showMenu.value = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Block User", color = Color.Red) },
                    onClick = {
                        chatManager.blockChatUser()
                        alertTitle.value = "User Blocked"
                        alertMessage.value = "The user has been blocked and you will no longer be able to message them. If this was a mistake, please contact justinlaiti22@rcsi.ie to unblock the user."
                        showAlert.value = true
                        showMenu.value = false
                    }
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .navigationBarsPadding()
        ) {
            items(messages.size) { index ->
                val message = messages[index]
                MessageItem(message = message, isCurrentUser = message.currentUserID == currentUser?.id)
            }
        }

        Row (
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            // Message input
            OutlinedTextField(
                value = newMessageText.value,
                onValueChange = { newMessageText.value = it },
                modifier = Modifier
                    .weight(1f),
                label = { Text("Type a message") }
            )

            IconButton(
                onClick = {
                    if(newMessageText.value.trim().isNotEmpty()) {
                        chatManager.sendMessage(newMessageText.value.trim()) // Update this with your method to send message
                        newMessageText.value = "" // Clear the input field
                    }
                    userManager.clickedOn("send coach a message")
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message"
                )
            }
        }

        if (showAlert.value) {
            AlertDialog(
                onDismissRequest = { showAlert.value = false },
                title = { Text(alertTitle.value) },
                text = { Text(alertMessage.value) },
                confirmButton = {
                    TextButton(onClick = { showAlert.value = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun MessageItem(message: Message, isCurrentUser: Boolean) {
    val maxWidth = getScreenWidthFraction(0.7f)
    val annotatedString = detectLinks(message.text)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        ClickableText(
            text = annotatedString,
            style = TextStyle(
                color = Color.White,
                fontSize = MaterialTheme.typography.body1.fontSize,
                fontWeight = FontWeight.Normal
            ),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        openUrl(annotation.item, context)
                    }

            },
            modifier = Modifier
                .background(
                    color = if (isCurrentUser) Color(0xFF2196F3) else Color.LightGray,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .widthIn(max = maxWidth)
                .align(if (isCurrentUser) Alignment.End else Alignment.Start)

        )
    }
}

fun openUrl(url: String, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

@Composable
fun getScreenWidthFraction(fraction: Float): Dp {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    return (screenWidthDp * fraction)
}

fun detectLinks(text: String): AnnotatedString {
    val result = buildAnnotatedString {
        val matcher = Patterns.WEB_URL.matcher(text)
        var lastEnd = 0
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            append(text.substring(lastEnd, start))
            val link = text.substring(start, end)
            pushStringAnnotation(tag = "URL", annotation = link)
            withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                append(link)
            }
            pop()
            lastEnd = end
        }
        append(text.substring(lastEnd))
    }
    return result
}
