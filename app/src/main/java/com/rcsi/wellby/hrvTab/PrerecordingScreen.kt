package com.rcsi.wellby.hrvTab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rcsi.wellby.hrvTab.viewModels.BluetoothController
import com.rcsi.wellby.hrvTab.viewModels.HRVDataManager

enum class RecordingType {
    TIMER_ONLY, BREATH_PACER, RAW_DATA
}

@Composable
fun PreRecordingScreen(
    navController: NavController,
    bluetoothController: BluetoothController,
    hrvDataManager: HRVDataManager
) {
    val isConnected by bluetoothController.connectionState.collectAsState()
    var selectedRecordingType by remember { mutableStateOf<RecordingType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What type of recording would you like to start?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        RecordingOptions(selectedRecordingType) { selectedRecordingType = it }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                selectedRecordingType?.let {
                    hrvDataManager.setRecordingType(selectedRecordingType)
                    bluetoothController.startRecording()
                    navController.navigate("recordingScreen")
                }
            },
            enabled = isConnected == BluetoothController.ConnectionState.Connected && selectedRecordingType != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Start Recording")
        }
    }
}

@Composable
fun RecordingOptions(
    selectedType: RecordingType?,
    onOptionSelected: (RecordingType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RecordingOptionButton(
            type = RecordingType.TIMER_ONLY,
            label = "Timer Only",
            icon = Icons.Default.Timer,
            isSelected = selectedType == RecordingType.TIMER_ONLY,
            onOptionSelected = onOptionSelected
        )
        RecordingOptionButton(
            type = RecordingType.BREATH_PACER,
            label = "Breath Pacer",
            icon = Icons.Default.WindPower,
            isSelected = selectedType == RecordingType.BREATH_PACER,
            onOptionSelected = onOptionSelected
        )
        RecordingOptionButton(
            type = RecordingType.RAW_DATA,
            label = "Raw Data",
            icon = Icons.Default.AutoGraph,
            isSelected = selectedType == RecordingType.RAW_DATA,
            onOptionSelected = onOptionSelected
        )
    }
}

@Composable
fun RecordingOptionButton(
    type: RecordingType,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onOptionSelected: (RecordingType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOptionSelected(type) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}