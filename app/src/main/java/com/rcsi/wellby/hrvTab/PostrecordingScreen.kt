package com.rcsi.wellby.hrvTab
// screen which is displayed after the recording is completed that asks what activity the user is doing
// and collects their check-in information before finishing

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rcsi.wellby.hrvTab.models.HRVSessionData
import com.rcsi.wellby.hrvTab.viewModels.BluetoothController
import com.rcsi.wellby.hrvTab.viewModels.HRVDataManager
import com.rcsi.wellby.signinSystem.AuthManager
import com.rcsi.wellby.toolkitTab.checkIn.CheckInData
import com.rcsi.wellby.toolkitTab.checkIn.CheckInEntryBlock
import com.rcsi.wellby.toolkitTab.checkIn.CheckInManager
import com.rcsi.wellby.toolkitTab.checkIn.ExtendedCheckIn
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PostRecordingScreen(
    navController: NavController,
    hrvDataManager: HRVDataManager,
    authManager: AuthManager,
    bluetoothController: BluetoothController
) {
    val checkInManager = viewModel<CheckInManager>()

    val currentUser by authManager.currentUser.collectAsState()
    val userId = currentUser?.id ?: ""

    var selectedActivities by remember { mutableStateOf(setOf<String>()) }
    val activities = listOf("At School", "Exercising", "Studying", "Relaxing", "With Friends", "With Family", "Eating", "Commuting", "Working")

    val mood by remember { mutableStateOf("") }
    val alertSelected by remember { mutableIntStateOf(0) }
    val calmSelected by remember { mutableIntStateOf(0) }
    val customReason = remember { mutableStateOf("") }
    val customAction = remember { mutableStateOf("") }

    val isFormComplete = mood.isNotEmpty() && alertSelected > 0 && calmSelected > 0

    val participantID by authManager.currentUser.collectAsState() // Collect user ID from AuthManager
    val sessionID = bluetoothController.sessionID.toString() // Retrieve session ID from BluetoothController
    val isProcessingData by hrvDataManager.isProcessingData.collectAsState()

    // Track when to navigate
    val showExtendedCheckIn = remember { mutableStateOf(true) }

    LaunchedEffect(showExtendedCheckIn.value) {
        if (!showExtendedCheckIn.value) {
            navController.navigate("biofeedback")
        }
    }

    LaunchedEffect(Unit) {
        val user = participantID // Assuming participantID is an object
        if (user != null && user.id.isNotEmpty()) { // Check if user object is not null and has a valid ID
            hrvDataManager.remotePpgProcessing(user.id, sessionID) { result ->
                if (result != null) {
                    Log.d("PostRecording", "Received PPG data: $result")
                    // Handle the result, e.g., update UI or save metrics
                } else {
                    Log.e("PostRecording", "Failed to fetch PPG data")
                }
            }
            hrvDataManager.uploadCalibrationData(userId = user.id, recordingId = sessionID)
        } else {
            Log.e("PostRecording", "User ID not found. Cannot process PPG.")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        ExtendedCheckIn(
            checkInManager = checkInManager,
            userId = userId,
            mood = remember { mutableStateOf(mood) },
            relaxedSlider = remember { mutableStateOf(calmSelected.toFloat()) },
            alertSlider = remember { mutableStateOf(alertSelected.toFloat()) },
            customReason = customReason,
            customAction = customAction,
            showExtendedCheckIn = showExtendedCheckIn
        )
    }

}


//
//    Text("What's going on?", style = MaterialTheme.typography.headlineSmall)
//    FlowRow(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceEvenly
//    ) {
//        activities.forEach { activity ->
//            val isSelected = activity in selectedActivities
//            FilterChip(
//                selected = isSelected,
//                onClick = {
//                    selectedActivities = if (isSelected) selectedActivities - activity else selectedActivities + activity
//                },
//                label = { Text(activity) }
//            )
//        }
//    }