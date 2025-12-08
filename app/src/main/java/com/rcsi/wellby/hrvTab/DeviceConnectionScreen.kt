package com.rcsi.wellby.hrvTab
// view for managing connection to a wearable device when the icon is selected on the main screen

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.rcsi.wellby.hrvTab.viewModels.BluetoothController

@Composable
fun DeviceConnectionScreen(bluetoothController: BluetoothController) {
    val devices by bluetoothController.devices.collectAsState()
    val connectionState by bluetoothController.connectionState.collectAsState()
    val isScanning by bluetoothController.isScanning.collectAsState()
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isScanning) bluetoothController.startScanning()
                    else bluetoothController.stopScanning()
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(if (isScanning) "Stop Scanning" else "Start Scanning")
            }

            DeviceConnectionStatus(connectionState, bluetoothController)

            if (isScanning) {
                DeviceList(devices, context, bluetoothController)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

}

@Composable
fun DeviceConnectionStatus(connectionState: BluetoothController.ConnectionState, bluetoothController: BluetoothController) {
    if (connectionState == BluetoothController.ConnectionState.Connected) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Connected to: ${bluetoothController.connectedDeviceName()}")
            Button(
                onClick = { bluetoothController.disconnect() },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Disconnect")
            }
        }
    }
}

@Composable
fun DeviceList(devices: List<BluetoothDevice>, context: Context, bluetoothController: BluetoothController) {
    LazyColumn {
        items(devices) { device ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = device.name ?: "Unknown Device")
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                            bluetoothController.connectToDevice(device)
                        } else {
                            Toast.makeText(context, "BLUETOOTH_CONNECT permission not granted", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Connect")
                }
            }
        }
    }
}