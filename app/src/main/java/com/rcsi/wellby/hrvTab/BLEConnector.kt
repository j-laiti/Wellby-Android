// Main screen for this tab which includes an icon control of BLE, the title, recent summary metrics,
// and a breath pacer and info box
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WatchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rcsi.wellby.hrvTab.viewModels.BluetoothController
import com.rcsi.wellby.hrvTab.viewModels.HRVDataManager
import com.rcsi.wellby.hrvTab.models.DisplayMetric
import com.rcsi.wellby.signinSystem.AuthManager
import kotlinx.coroutines.launch

@Composable
fun BLEConnector(
    bluetoothController: BluetoothController,
    navController: NavController,
    hrvDataManager: HRVDataManager,
    userManager: AuthManager
) {
    val isScanning by bluetoothController.isScanning.collectAsState()
    val metrics by hrvDataManager.latestMetrics.collectAsState()
    val userId = userManager.currentUser.collectAsState().value?.id ?: ""

    LaunchedEffect(key1 = bluetoothController) {
        hrvDataManager.fetchLatestHRVData(userId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            ConnectionIcon(bluetoothController, navController)

            HRVTitle()

            Spacer(modifier = Modifier.weight(1f))

            MetricsBlock(metrics, navController, bluetoothController, hrvDataManager, userManager)

            Spacer(modifier = Modifier.weight(1f))

            BottomButtonOptions(navController)

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ConnectionIcon(bluetoothController: BluetoothController, navController: NavController) {
    val connectionState by bluetoothController.connectionState.collectAsState()

    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(
            onClick = { navController.navigate("deviceConnectionScreen") },
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = if (connectionState == BluetoothController.ConnectionState.Connected) {
                    Icons.Filled.Watch
                } else {
                    Icons.Filled.WatchOff
                },
                contentDescription = if (connectionState == BluetoothController.ConnectionState.Connected) {
                    "Device Connected"
                } else {
                    "No Device Connected"
                },
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun HRVTitle() {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "Wearable Dashboard",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun MetricsBlock(
    metrics: List<DisplayMetric>,
    navController: NavController,
    bluetoothController: BluetoothController,
    hrvDataManager: HRVDataManager,
    userManager: AuthManager
    ) {
    val showInfoDialog = remember { mutableStateOf(false) }
    val elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)

    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(4.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        elevation = elevation
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Measures:", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showInfoDialog.value = true }) {
                    Icon(Icons.Filled.Info, contentDescription = "Info")
                }
            }

            MetricsInfoDialog(
                showDialog = showInfoDialog,
                onDismiss = { showInfoDialog.value = false })

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(metrics) { metric ->
                    MetricItem(metric)
                }
            }

            ActionButtons(navController, bluetoothController)

            RelaxationScale(hrvDataManager, userManager)

        }
    }
}

@Composable
fun ActionButtons(navController: NavController, bluetoothController: BluetoothController) {
    val connectionState by bluetoothController.connectionState.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                if (connectionState == BluetoothController.ConnectionState.Connected) {
                    navController.navigate("prerecording")
                }
            },
            enabled = connectionState == BluetoothController.ConnectionState.Connected
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Start Recording", modifier = Modifier.size(24.dp))
            Text("Start New Recording", modifier = Modifier.padding(start = 8.dp))
        }

        Button(
            onClick = { navController.navigate("sessionSummary") }
        ) {
            Icon(Icons.Filled.Summarize, contentDescription = "Session Summary", modifier = Modifier.size(24.dp))
            Text("View Session Summary", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun RelaxationScale(hrvDataManager: HRVDataManager, userManager: AuthManager) {
    val currentUser by userManager.currentUser.collectAsState() // Observe user state
    val calibrationCount by hrvDataManager.calibrationRecordCount.collectAsState() // Observe calibration progress
    val stressEstimator by hrvDataManager.stressEstimator.collectAsState()

    LaunchedEffect(currentUser?.id) { // React only when the user ID changes
        currentUser?.id?.let { userId ->
            hrvDataManager.checkCalibrationProgress(userId) // Trigger the calibration progress check
        }
    }

    // UI to display calibration progress
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (calibrationCount < 4) {
            Text(text = "Calibration Recordings:")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) { index ->
                    Circle(
                        color = if (index < calibrationCount) MaterialTheme.colorScheme.primary else Color.Gray,
                        isFilled = index < calibrationCount
                    )
                }
            }
        } else {
            Text(text = "Estimated Relaxation")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                val relaxProbability = 1 - (stressEstimator ?: 0.5) // Swap scale; default 0.5 if null
                val dotPosition = relaxProbability.coerceIn(0.0, 1.0)

                LinearGradientScale()
                DotIndicator(dotPosition)
            }
        }
    }
}

@Composable
fun Circle(color: Color, isFilled: Boolean, size: Dp = 25.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(color = if (isFilled) color else Color.Transparent, shape = CircleShape)
            .border(BorderStroke(2.dp, color), shape = CircleShape)
    )
}

@Composable
fun LinearGradientScale() {
    Box(
        modifier = Modifier
            .height(20.dp)
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Red, Color.Yellow, Color.Green)
                ),
                shape = RoundedCornerShape(10.dp)
            )
    )
}

@Composable
fun DotIndicator(position: Double) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val maxWidth = (constraints.maxWidth.toFloat() / 3) // Get the maximum width in pixels
        val dotPosition = (position * maxWidth) // Ensure the position stays within bounds

        Box(
            modifier = Modifier
                .size(20.dp)
                .offset(x = dotPosition.dp - 15.dp) // Adjust for centering the dot
                .background(Color.White, shape = CircleShape)
                .shadow(5.dp, shape = CircleShape)
        )
    }
}

//@Composable
//fun DotIndicator(position: Double) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 5.dp),
//        contentAlignment = Alignment.CenterStart
//    ) {
////        maxwidth = maxWidth
//        Box(
//            modifier = Modifier
//                .size(20.dp)
//                .offset(x = (position * 225).dp - 10.dp) // Adjust for centering the dot
//                .background(Color.White, shape = CircleShape)
//                .shadow(5.dp, shape = CircleShape)
//        )
//    }
//}

@Composable
fun MetricItem(metric: DisplayMetric) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(imageVector = metric.icon, contentDescription = metric.name)
        Text(
            text = metric.value,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = metric.name,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun MetricsInfoDialog(showDialog: MutableState<Boolean>, onDismiss: () -> Unit) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("HRV Metrics Information") },
            text = { Text("'Calming Response' shows relaxation levels while 'Return to Balance' indicates the balance between activity and relaxation in your body. Higher values for each suggest more relaxation/balance. HRV can be confusing, so check out the HRV Info section for more clarity!") },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun BottomButtonOptions(navController: NavController) {
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ElevatedButton(
            onClick = { navController.navigate("breathPacer") },
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSecondary,
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Air,
                    contentDescription = "Breath Pacer",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Breath",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Pacer",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        ElevatedButton(
            onClick = { navController.navigate("hrvInfo") },
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSecondary,
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.QuestionAnswer,
                    contentDescription = "HRV Information",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "HRV",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Information",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
