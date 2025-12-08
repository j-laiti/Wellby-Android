package com.rcsi.wellby
// notification permission checks and confirmation dialog for permission status
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat

@Composable
fun NotificationPermissionRequester() {
    val context = LocalContext.current
    val notificationManagerCompat = NotificationManagerCompat.from(context)
    val areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled()

    var showPermissionRationale by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                showPermissionRationale = true
            } else {
                //schedule notification here??
                scheduleCheckInReminders(context)
            }
        }
    )

    // This LaunchedEffect will only run once when the Composable is first put into the UI
    LaunchedEffect(key1 = Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !areNotificationsEnabled) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (showPermissionRationale) {
        // This dialog will show if the user has denied the permission once
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Notification Permission Confirmation") },
            text = { Text("Notifications are used for new messages from your coach and for twice a week check-ins.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionRationale = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !areNotificationsEnabled) {
                        launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)  // Re-request permission
                    }
                }) {
                    Text("Show notification options")
                }
            },
            dismissButton = {
                Button(onClick = { showPermissionRationale = false }) {
                    Text("No Thanks")
                }
            }
        )
    }
}