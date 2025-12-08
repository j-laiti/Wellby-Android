package com.rcsi.wellby.hrvTab
// screen displayed during the recording which is either a breath pacer or a beating heart along
// with a 60 second countdown
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rcsi.wellby.hrvTab.viewModels.BluetoothController
import com.rcsi.wellby.hrvTab.viewModels.HRVDataManager
import kotlinx.coroutines.delay
import kotlin.io.path.Path

@Composable
fun RecordingScreen(
    bluetoothController: BluetoothController,
    hrvDataManager: HRVDataManager,
    navController: NavController
) {
    val recordingType = hrvDataManager.recordingType.collectAsState().value
    val rawPPGReadings by bluetoothController.rawPPGReadings.collectAsState()
    var timeLeft by remember { mutableStateOf(60) } // 60 seconds countdown

    LaunchedEffect(key1 = true) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
        }
        navController.navigate("postrecording")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Exit button at the top-right corner
        IconButton(
            onClick = {
                bluetoothController.stopRecording()
                navController.navigate("biofeedback") // Navigate to biofeedback screen
            },
            modifier = Modifier.align(Alignment.TopEnd) // Align the button to the top-right
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Exit Recording",
                tint = MaterialTheme.colorScheme.error
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            when (recordingType) {
                RecordingType.TIMER_ONLY -> HeartBeatAnimation()
                RecordingType.BREATH_PACER -> BreathPacer()
                RecordingType.RAW_DATA -> {
                    if (rawPPGReadings.isNotEmpty()) {
                        RawPPGGraph(rawPPGReadings)
                    } else {
                        Text("No PPG data available.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                else -> Text("Recording type not selected.", style = MaterialTheme.typography.bodyLarge)
            }

            Text(
                "Recording heart activity...", style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(8.dp)
            )

            LinearProgressIndicator(
                progress = 1f - timeLeft / 60f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

        }
    }
}

@Composable
fun HeartBeatAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Icon(
        imageVector = Icons.Filled.Favorite,
        contentDescription = "Heartbeat",
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        tint = MaterialTheme.colorScheme.error
    )
}

@Composable
fun BreathPacer() {
    val scale = remember { Animatable(1f) } // This will handle the scaling of the circle
    var breathStatus by remember { mutableStateOf("Breathe In") } // Initially set to "Breathe In"
    val breathInTime = 5000L
    val breathOutTime = 5000L

    LaunchedEffect(key1 = "breathPacer") {
        while (true) {
            // Breathe in
            breathStatus = "Breathe In"
            scale.animateTo(targetValue = 3f, animationSpec = tween(breathInTime.toInt()))
            //delay(breathInTime)

            // Breathe out
            breathStatus = "Breathe Out"
            scale.animateTo(targetValue = 1f, animationSpec = tween(breathOutTime.toInt()))
            //delay(breathOutTime)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = breathStatus,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Canvas(
            modifier = Modifier
                .size(200.dp)
                .aspectRatio(1f)
        ) {
            drawCircle(
                color = Color.Blue,
                radius = size.minDimension / 8 * scale.value
            )
        }
    }
}

@Composable
fun RawPPGGraph(readings: List<Double>) {
    val maxY = readings.maxOrNull() ?: 1.0
    val minY = readings.minOrNull() ?: 0.0
    val rangeY = maxY - minY
    val normalizedReadings = readings.map { (it - minY) / (rangeY.coerceAtLeast(1.0)) }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        val path = androidx.compose.ui.graphics.Path().apply {
            normalizedReadings.forEachIndexed { index, value ->
                val x = (size.width / (normalizedReadings.size - 1)) * index
                val y = size.height - (value * size.height).toFloat()
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        drawPath(path, color = Color.Blue, style = Stroke(width = 3f))
    }
}
