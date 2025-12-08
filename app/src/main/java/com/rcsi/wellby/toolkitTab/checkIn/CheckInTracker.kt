package com.rcsi.wellby.toolkitTab.checkIn
// summary view of check-in entries which includes a list of mood emojis and a graph of entries

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rcsi.wellby.signinSystem.AuthManager

@Composable
fun CheckInTracker(authManager: AuthManager) {
    val currentUser by authManager.currentUser.collectAsState()
    val userId = currentUser?.id ?: ""
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        authManager.viewDidAppear("Checkin Summary")
    }

    Column (
        modifier = Modifier.fillMaxSize(1f)
            .background(
                Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), Color.White),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY  // This makes the gradient stretch to the bottom
                )
            ).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Check-in Tracker",
            style = MaterialTheme.typography.headlineMedium
        )

        TrackerBlock(userId, userManager = authManager)

        Button(onClick = {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.6seconds.org/2022/03/13/plutchik-wheel-emotions/"))
            context.startActivity(intent)
            authManager.clickedOn("emotion wheel")
        },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Emotion Wheel")
        }
    }

}