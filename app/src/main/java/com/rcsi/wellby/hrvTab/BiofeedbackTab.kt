package com.rcsi.wellby.hrvTab
// Main screen of the biofeedback/heart activity tab which first asks for bluetooth permissions to
// be enabled and then runs the main screen for the tab (BLEConnector)
import BLEConnector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rcsi.wellby.hrvTab.helpers.PermissionHandler
import com.rcsi.wellby.hrvTab.viewModels.BluetoothController
import com.rcsi.wellby.hrvTab.viewModels.HRVDataManager
import com.rcsi.wellby.signinSystem.AuthManager

@Composable
fun BiofeedbackTab(
    bluetoothController: BluetoothController,
    navController: NavController,
    hrvDataManager: HRVDataManager,
    userManager: AuthManager,
    permissionHandler: PermissionHandler
) {
    // A state to track whether Bluetooth is ready
    val (isBluetoothReady, setBluetoothReady) = remember { mutableStateOf(false) }

    // track loading status of processing
    val isProcessingData by hrvDataManager.isProcessingData.collectAsState()

    LaunchedEffect(key1 = Unit) {
        // Request necessary permissions
        permissionHandler.checkAndRequestPermissions()

        // Wait for Bluetooth to be enabled, and update state based on the result
        val isBluetoothEnabled = permissionHandler.enableBluetooth()
        setBluetoothReady(isBluetoothEnabled)

        userManager.viewDidAppear("Biofeedback")
    }

    // UI displayed based on the state of Bluetooth and data processing
    when {
        !isBluetoothReady -> {
            Text("Waiting for Bluetooth to be enabled...")
        }
        isProcessingData -> {
            // Show loading animation and message while processing
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Processing data, please wait...")
            }
        }
        else -> {
            BLEConnector(bluetoothController, navController, hrvDataManager, userManager)
        }
    }


    DisposableEffect(key1 = Unit) {
        onDispose {
            bluetoothController.stopScanning()
        }
    }
}