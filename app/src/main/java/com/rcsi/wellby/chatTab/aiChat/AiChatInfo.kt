package com.rcsi.wellby.chatTab.aiChat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rcsi.wellby.signinSystem.AuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatInfo(userManager: AuthManager) {

    LaunchedEffect(key1 = Unit) {
        userManager.viewDidAppear("AI chat info")
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Wellby AI Info") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            InfoSection(
                title = "What you can discuss:",
                content = """
                    - Stress management
                    - Sleep hygiene
                    - Digital well-being
                    - Healthy eating
                    - Maintaining healthy relationships
                    - Alcohol and tobacco use management
                    - Time management
                """.trimIndent()
            )

            InfoSection(
                title = "Is this a therapeutic or medical service?",
                content = """
                    No, this chat is not a therapeutic or medical service. If you need professional support, please talk to someone you trust or consult a professional. 
                    See the 'Further Support' section for links to free resources.
                """.trimIndent()
            )

            InfoSection(
                title = "Are my messages confidential?",
                content = """
                    Conversations are not saved or monitored by the RCSI research team, but data is processed through OpenAI. Only the most recent 20 messages are saved for you to view.
                """.trimIndent()
            )

            InfoSection(
                title = "What happens if I mention safety concerns?",
                content = """
                    If you mention something that suggests harm to yourself or others, the message may be flagged for review. Flagged messages could be shared with your school to ensure your safety and well-being.
                    
                    All non-flagged messages remain confidential and are not saved by RCSI.
                """.trimIndent()
            )
        }
    }
}

@Composable
fun InfoSection(title: String, content: String) {

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
