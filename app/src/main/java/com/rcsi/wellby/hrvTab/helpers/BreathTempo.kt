package com.rcsi.wellby.hrvTab.helpers
// interactive view with a circular breath pacer and custom time options

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.rcsi.wellby.signinSystem.AuthManager
import kotlinx.coroutines.delay

@Composable
fun BreathTempo(userManager: AuthManager) {
    var breathStatus by remember { mutableStateOf("") }
    var isHold by remember { mutableStateOf(false) }
    val lastScale = remember { mutableStateOf(1f) }

    var selectedExercise by remember { mutableStateOf(BreathingExercise.Custom) }
    var showMenu by remember { mutableStateOf(false) }

    var breathInTime by remember { mutableFloatStateOf(4f) } // Default 4 seconds
    var breathOutTime by remember { mutableFloatStateOf(4f) } // Default 4 seconds
    var hold1Time by remember { mutableFloatStateOf(2f) } // Default 2 seconds
    var hold2Time by remember { mutableFloatStateOf(2f) }

    val primaryColor = MaterialTheme.colorScheme.primary

    val targetScale = when {
        breathStatus == "Breathe In" -> 3f
        breathStatus == "Breathe Out" -> 1f
        breathStatus == "" -> 1f
        isHold -> lastScale.value
        else -> 1f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            durationMillis = when (breathStatus) {
                "Breathe In", "Breathe Out" -> ((if (breathStatus == "Breathe In") breathInTime else breathOutTime) * 1000f).toInt()
                else -> 0
            }
        ),
        finishedListener = { newValue ->
            lastScale.value = newValue
        },
        label = "BreathingAnimation"
    )

    LaunchedEffect(key1 = Unit) {
        userManager.viewDidAppear("Breath Pacer")
    }

    Column(
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), Color.White),
                startY = 0f,
                endY = Float.POSITIVE_INFINITY  // This makes the gradient stretch to the bottom
            )
        )
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(if (isHold) "Hold" else breathStatus, style = MaterialTheme.typography.headlineMedium)

        Canvas(modifier = Modifier
            .aspectRatio(1f)
        ) {
            drawCircle(
                color = primaryColor,
                radius = size.minDimension / 8 * animatedScale
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = selectedExercise.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable { showMenu = !showMenu }
                    .padding(end = 8.dp)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown, // Ensure you've imported Icons and ArrowDropDown
                contentDescription = "Dropdown",
                modifier = Modifier.clickable { showMenu = !showMenu }
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(x = 120.dp, y = 0.dp)
        ) {
            BreathingExercise.entries.forEach { exercise ->
                DropdownMenuItem(text = {
                    Text(exercise.title)
                }, onClick = {
                    selectedExercise = exercise
                    breathInTime = exercise.breathIn
                    hold1Time = exercise.hold1
                    breathOutTime = exercise.breathOut
                    hold2Time = exercise.hold2
                    showMenu = false
                })
            }
        }

        BreathSlider(
            title = "Breath in",
            time = breathInTime,
            onTimeChange = { breathInTime = it },
            rangeStart = 1f,
            steps = 17
        )

        BreathSlider(
            title = "Hold 1",
            time = hold1Time,
            onTimeChange = { hold1Time = it },
            rangeStart = 0f,
            steps = 19
        )

        BreathSlider(
            title = "Breath out",
            time = breathOutTime,
            onTimeChange = { breathOutTime = it },
            rangeStart = 1f,
            steps = 17
        )

        BreathSlider(
            title = "Hold 2",
            time = hold2Time,
            onTimeChange = { hold2Time = it },
            rangeStart = 0f,
            steps = 19
        )
    }

    LaunchedEffect(breathStatus) {
        when (breathStatus) {
            "Breathe In" -> {
                delay((breathInTime * 1000).toLong())
                isHold = true
                delay((hold1Time * 1000).toLong())
                isHold = false
                breathStatus = "Breathe Out"
            }
            "Breathe Out" -> {
                delay((breathOutTime * 1000).toLong())
                isHold = true
                delay((hold2Time * 1000).toLong())
                isHold = false
                breathStatus = "Breathe In"
            }
            "" -> {
                breathStatus = "Breathe In"
            }
        }
    }
}

@Composable
fun BreathSlider(title: String, time: Float, onTimeChange: (Float) -> Unit, rangeStart: Float, steps: Int) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(80.dp))
        Slider(
            value = time,
            onValueChange = onTimeChange,
            valueRange = rangeStart..10f,
            steps = steps,
            modifier = Modifier.weight(1f)
        )
        Text(text = String.format("%.1f s", time), style = MaterialTheme.typography.titleMedium)
    }
}

enum class BreathingExercise(val title: String, val breathIn: Float, val hold1: Float, val breathOut: Float, val hold2: Float) {
    Custom("Custom",4f, 2f, 4f, 2f),
//    BoxBreathing("Box Breathing", 4f, 4f, 4f, 4f),
//    ResonantBreathing("Resonant Breathing", 5.5f, 0f, 5.5f, 0f),
//    DeepBreathing("Deep Breathing", 4f, 6f, 8f, 6f)
}