package com.rcsi.wellby.chatTab.aiChat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.runtime.LaunchedEffect
import com.rcsi.wellby.signinSystem.AuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FurtherResourcesView(userManager: AuthManager) {

    LaunchedEffect(key1 = Unit) {
        userManager.viewDidAppear("Further supports")
    }

    val resources = listOf(
        Triple(
            "Jigsaw",
            "https://jigsaw.ie/",
            "Free mental health support for young people, offering advice, online chats, and in-person services."
        ),
        Triple(
            "Childline",
            "https://www.childline.ie/",
            "A free, confidential, and 24/7 support service for young people, available via phone, chat, or text."
        ),
        Triple(
            "Belong To",
            "https://www.belongto.org/",
            "Support for LGBTQ+ young people in Ireland, promoting mental health, acceptance, and advocacy."
        ),
        Triple(
            "spunout",
            "https://spunout.ie/",
            "Ireland’s youth information hub offering articles, resources, and support on mental health, education, and wellbeing."
        ),
        Triple(
            "HSE Recommended Mental Health Services",
            "https://www2.hse.ie/mental-health/services-support/supports-services/",
            "Ireland's public mental health services, providing information on available supports and guidance for accessing care."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Further Supports") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            resources.forEach { resource ->
                ResourceItem(title = resource.first, url = resource.second, description = resource.third)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ResourceItem(title: String, url: String, description: String) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open Link",
                    tint = Color.Blue,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        ContextCompat.startActivity(context, intent, null)
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
