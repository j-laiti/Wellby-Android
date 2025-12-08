package com.rcsi.wellby.toolkitTab
// screen with settings including user information, app theme color customisation, logging out and deleting account options
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.lerp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.rcsi.wellby.signinSystem.AuthManager
import com.rcsi.wellby.signinSystem.User
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.rcsi.wellby.toolkitTab.ColorPicker.ColorPickerView
import com.rcsi.wellby.toolkitTab.ColorPicker.DataStoreManager
import com.rcsi.wellby.toolkitTab.ColorPicker.DataStoreManager.displayQuoteFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.rcsi.wellby.toolkitTab.ColorPicker.DataStoreManager.selectedPrimaryColorFlow
import com.rcsi.wellby.toolkitTab.ColorPicker.DataStoreManager.selectedSecondaryColorFlow
import com.rcsi.wellby.toolkitTab.ColorPicker.ProvideAppColors

@Composable
fun SettingsScreen(userManager: AuthManager) {
    val currentUser = userManager.currentUser.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val displayQuote by context.displayQuoteFlow.collectAsState(initial = true)

    // Track the opt-in toggle state
    val isCoachingOptedIn = remember { mutableStateOf(currentUser?.isCoachingOptedIn ?: false) }
    val showConfirmationDialog = remember { mutableStateOf(false) }


    LazyColumn(
        modifier = Modifier.padding(all = 12.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {

        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        item {
            Text(
                text = "User Information",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 10.dp).padding(top = 10.dp),
                color = Color.Gray
            )
        }

        item {
            SectionCard {
                UserInformationSection(currentUser)
            }
        }

        item {
            SectionCard {
                FeedbackLink(userManager)
            }
        }

        item {
            Text(
                text = "Colour Selection",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 10.dp).padding(top = 5.dp),
                color = Color.Gray
            )
        }

        item {
            SectionCard {
                CustomColorsSection(userManager = userManager)
            }
        }

        item {
            Text(
                text = "Home Screen Customisation",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 10.dp).padding(top = 10.dp),
                color = Color.Gray
            )
        }

        item {
            SectionCard {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Daily Quote", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = displayQuote,
                        onCheckedChange = { newValue ->
                            coroutineScope.launch {
                                DataStoreManager.saveDisplayQuoteSetting(context, newValue)
                            }
                            userManager.clickedOn("Display Quote")
                        }
                    )
                }
            }
        }

        item {
            Text(
                text = "Chat Tab",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 10.dp).padding(top = 10.dp),
                color = Color.Gray
            )
        }

        item {
            SectionCard {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Access a Human Health Coach", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = isCoachingOptedIn.value,
                        onCheckedChange = { newValue ->
                            isCoachingOptedIn.value = newValue
                            showConfirmationDialog.value = true
                        }
                    )
                }
            }

            // Confirmation dialog for opt-in/out
            if (showConfirmationDialog.value) {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog.value = false },
                    title = { Text("Confirm Coaching") },
                    text = {
                        Text(
                            if (isCoachingOptedIn.value) {
                                "Opting in to coaching will assign you a human coach. Do you want to continue?"
                            } else {
                                "Opting out will stop your access to health coaching. Are you sure you want to proceed?"
                            }
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            coroutineScope.launch {
                                userManager.applyOptInChange(isCoachingOptedIn.value)
                                showConfirmationDialog.value = false
                            }
                        }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showConfirmationDialog.value = false
                                isCoachingOptedIn.value = !isCoachingOptedIn.value
                            }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }


        item {
            Text(
                text = "Logging Out",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 10.dp).padding(top = 10.dp),
                color = Color.Gray
            )
        }

        item {
            LoggingOutSection(userManager)
        }

        item {
            Text(
                text = "Account Deletion",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 10.dp).padding(top = 10.dp),
                color = Color.Gray
            )
        }

        item {
            AccountDeletionSection(userManager)
        }
    }
}


@Composable
fun UserInformationSection(currentUser: User?) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "User Icon",
                modifier = Modifier.size(50.dp)
            )
            if (currentUser != null) {
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        "Name: ${currentUser.firstName} ${currentUser.surname}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Username: ${currentUser.username}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (currentUser.student) {
                        Text(
                            "School: ${currentUser.school}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                Text("Error Loading user data", color = Color.Red)
            }
        }
}

@Composable
fun CustomColorsSection(userManager: AuthManager) {
    val context = LocalContext.current
    var showPrimaryColorPickerDialog by remember { mutableStateOf(false) }
    var showSecondaryColorPickerDialog by remember { mutableStateOf(false) }
    val (selectedPrimaryColor, setSelectedPrimaryColor) = remember { mutableStateOf(Color.Blue) }
    val (selectedSecondaryColor, setSelectedSecondaryColor) = remember { mutableStateOf(Color.Green) }

    LaunchedEffect(Unit) {
        context.selectedPrimaryColorFlow.collect { hexColor ->
            hexColor?.let {
                setSelectedPrimaryColor(Color(android.graphics.Color.parseColor(it)))
            }
        }
        context.selectedSecondaryColorFlow.collect { hexColor ->
            hexColor?.let {
                setSelectedSecondaryColor(Color(android.graphics.Color.parseColor(it)))
            }
        }
    }

    Column(modifier = Modifier.padding(8.dp)) {
        // Primary color picker
        ColorPickerSection(
            title = "Change Main App Colour",
            showDialog = showPrimaryColorPickerDialog,
            setShowDialog = { showPrimaryColorPickerDialog = it },
            selectedColor = selectedPrimaryColor,
            setSelectedColor = {
                setSelectedPrimaryColor(it)
                val hexColor = "#${Integer.toHexString(it.toArgb()).substring(2)}"
                CoroutineScope(Dispatchers.IO).launch {
                    DataStoreManager.saveSelectedPrimaryColor(context, hexColor)
                }
            },
            userManager = userManager
        )

    }
}

@Composable
fun ColorPickerSection(
    title: String,
    showDialog: Boolean,
    setShowDialog: (Boolean) -> Unit,
    selectedColor: Color,
    setSelectedColor: (Color) -> Unit,
    userManager: AuthManager
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
    ) {
        TextButton(onClick = { setShowDialog(true) }) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .size(40.dp)
                .padding(4.dp)
                .background(color = selectedColor, shape = RoundedCornerShape(4.dp))
        )
    }

    if (showDialog) {
        ColorPickerDialog(
            selectedColor = selectedColor,
            onColorSelected = { color ->
                setSelectedColor(color)
                setShowDialog(false)
                userManager.clickedOn("color_1")
            },
            onDismissRequest = { setShowDialog(false) }
        )
    }
}
@Composable
fun ColorPickerDialog(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current  // Correct usage in Composable
    val controller = rememberColorPickerController()
    var tempColor by remember { mutableStateOf(selectedColor) }
    val coroutineScope = rememberCoroutineScope()  // Remember the coroutine scope tied to this Composable's lifecycle

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Choose a colour") },
        text = {
            Column {
                ColorPickerView(controller = controller) { colorEnvelope ->
                    tempColor = colorEnvelope.color  // Safely update temporary color
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onColorSelected(tempColor)  // Notify listener with the new color
                    val hexColor = "#${Integer.toHexString(tempColor.toArgb()).substring(2)}"
                    coroutineScope.launch {  // Use coroutineScope tied to the Composable's lifecycle
                        DataStoreManager.saveSelectedPrimaryColor(context, hexColor)
                    }
                    onDismissRequest()  // Close the dialog
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}




@Composable
fun LoggingOutSection(userManager: AuthManager) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val lighterError = lerp(MaterialTheme.colorScheme.error, Color.White, 0.3f)

    SectionCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(
                onClick = { showLogoutDialog = true }
            ) {
                Text(
                    "Logout",
                    color = lighterError,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Log out") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    TextButton(onClick = {
                        userManager.logout()
                        showLogoutDialog = false
                    }) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}



@Composable
fun AccountDeletionSection(userManager: AuthManager) {
    var showDeleteAlert by remember { mutableStateOf(false) }

    SectionCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(
                onClick = { showDeleteAlert = true }
            ) {
                Text(
                    "Delete Account",
                    color = Color(0xFFB00020), // Specific red color used for deletion actions
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showDeleteAlert) {
            AlertDialog(
                onDismissRequest = { showDeleteAlert = false },
                title = { Text("Delete account") },
                text = { Text("This will delete all data associated with your account and cannot be undone. Are you sure you want to proceed?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            userManager.deleteUserAccount()
                            showDeleteAlert = false
                        }
                    ) {
                        Text("Delete", color = Color(0xFFB00020))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAlert = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun FeedbackLink(authManager: AuthManager) {
    val context = LocalContext.current
    TextButton(
        onClick = {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://forms.office.com/r/xVPbQQUs51")
            )
            context.startActivity(intent) // Launch the intent
            authManager.clickedOn("emotion wheel") // Track the interaction
        },
        modifier = Modifier.padding(8.dp)
    ) {
        Text("Send Feedback on Wellby")
    }
}

