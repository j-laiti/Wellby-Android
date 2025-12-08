package com.rcsi.wellby.toolkitTab
// main screen for the home tab with a settings icon button, daily quote, welcome message, check-in block, calendar, and saved resource button
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rcsi.wellby.signinSystem.AuthManager
import com.rcsi.wellby.toolkitTab.ColorPicker.DataStoreManager.displayQuoteFlow
import com.rcsi.wellby.toolkitTab.calendar.WeekView
import com.rcsi.wellby.toolkitTab.checkIn.CheckInEntryBlock
import com.rcsi.wellby.toolkitTab.quote.DailyQuoteBlock
import com.rcsi.wellby.toolkitTab.toDoList.ToDoViewModel

@Composable
fun HomeScreen(authManager: AuthManager, navController: NavController, toDoViewModel: ToDoViewModel) {
    val currentUser by authManager.currentUser.collectAsState()
    val userFirstName = currentUser?.firstName ?: ""
    val userId = currentUser?.id ?: ""

    val displayQuote by LocalContext.current.displayQuoteFlow.collectAsState(initial = true)

    var mood by remember { mutableStateOf("") }
    var alertSelected by remember { mutableStateOf(0) }
    var calmSelected by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        authManager.viewDidAppear("home screen")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        Color.White
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY  // This makes the gradient stretch to the bottom
                )
            ),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .padding(all = 15.dp)
                .fillMaxWidth(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Row {
                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {
                    navController.navigate("settings")
                }) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "settings"
                    )
                }
            }

            Text(
                text = "Welcome, $userFirstName",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (displayQuote) {
            DailyQuoteBlock()
            Spacer(modifier = Modifier.weight(1f))
        }

        CheckInEntryBlock(
            userId = userId,
            navController = navController,
            mood = mood,
            onMoodChange = { mood = it },
            alertSelected = alertSelected,
            onAlertChange = { alertSelected = it } ,
            calmSelected = calmSelected,
            onCalmChange = { calmSelected = it }
        ) {
            mood = ""
            alertSelected = 0
            calmSelected = 0
        }

        Spacer(modifier = Modifier.weight(1f))

        WeekView(navController = navController, toDoViewModel = toDoViewModel)

        Spacer(modifier = Modifier.weight(1f))


        ElevatedButton(
            onClick = { navController.navigate("savedResources") },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSecondary,
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Icon(Icons.Filled.CollectionsBookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.padding(5.dp))
            Text(
                text = "Saved Resources",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}