package com.rcsi.wellby.toolkitTab.checkIn
// block used in the check-in tracker screen which displays a graph of a max of 5 entered relaxed and alert levels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.rcsi.wellby.signinSystem.AuthManager

@Composable
fun TrackerBlock(userId: String, userManager: AuthManager) {
    val checkInManager = viewModel<CheckInManager>()
    val checkInEntries = checkInManager.checkInEntries.collectAsState().value
    val fetchPreviousEnabled = checkInManager.canFetchPrevious.collectAsState().value
    val fetchNextEnabled = checkInManager.canFetchNext.collectAsState().value

    var selectedEntry by remember { mutableStateOf<CheckInData?>(null) }

    LaunchedEffect(key1 = checkInManager) {
        checkInManager.fetchCheckInEntries(userId)
    }

    var surfaceHeight by remember { mutableStateOf(0) }

    Surface(
        elevation = 5.dp,
        modifier = Modifier
            .padding(10.dp)
            .onSizeChanged { size ->
                surfaceHeight = size.height
            },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.onSecondary
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .wrapContentHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        checkInManager.fetchPreviousCheckInEntries(userId)
                        userManager.clickedOn("checkin tracker < button")
                              },
                    enabled = fetchPreviousEnabled
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Mood",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFEBC38A)
                )
                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { checkInManager.fetchNextCheckInEntries(userId) },
                    enabled = fetchNextEnabled
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next"
                    )
                }
            }

            Spacer(modifier = Modifier.padding(4.dp))

            MoodLabels(checkInEntries = checkInEntries)

            Spacer(modifier = Modifier.padding(4.dp))

            Row {
                Text(
                    "Alertness",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.Red)
                )
                Text(" & ", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Calmness",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.Blue),
                )
            }

            Spacer(modifier = Modifier.padding(12.dp))

            CheckInChart(checkInEntries = checkInEntries, onPointClick = { checkInData ->
                selectedEntry = checkInData
                userManager.clickedOn("mood check in entry detail button")
            })

            Text(
                text = "Tap on the date for full check-in details",
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .alpha(ContentAlpha.medium),
                color = Color.Gray
            )

        }

        DetailViewOverlay(
            entry = selectedEntry,
            measuredHeight = surfaceHeight,
            onDismiss = { selectedEntry = null },
        )
    }
}

@Composable
fun MoodLabels(checkInEntries: List<CheckInData>) {
//    val spacing by spacingLogic.collectAsState() // Collecting the spacing value for current use

    val moodEmojiMap = mapOf(
        "happy" to "😄",
        "calm" to "☺️",
        "stressed" to "😣",
        "bored" to "😐",
        "tired" to "🥱"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp) // You might adjust the height based on your UI design
    ) {
        val width = size.width
        val spacePerEntry = width / (checkInEntries.size + 1)

        val reversedEntries = checkInEntries.reversed()

        reversedEntries.forEachIndexed { index, entry ->
            val x = spacePerEntry * (index + 1)
            val firstMood = entry.mood.split(", ").firstOrNull()
            drawContext.canvas.nativeCanvas.drawText(
                moodEmojiMap[firstMood] ?: firstMood.orEmpty(),
                x,
                size.height / 2, // Centering mood vertically in the provided space
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = size.height / 2 // Adjust text size as needed
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun CheckInChart(checkInEntries: List<CheckInData>, onPointClick: (CheckInData) -> Unit) {
    // Determine the maximum value for Y axis
    val maxY = 5
    val minY = 1

    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }


    // Reverse the list so newest entries are on the right
    val reversedEntries = checkInEntries.reversed()

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Draw your chart here (e.g., using Canvas or any custom chart Composable)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(225.dp)
        ) {
            val width = size.width
            val height = size.height

            // Define how much space each entry should take up on the X axis
            val spacePerEntry = width / (checkInEntries.size + 1)

            // Draw horizontal lines for the chart grid
            for (i in minY..maxY) {
                val y = height - (height * i / maxY)
                drawLine(
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    color = Color.LightGray
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "$i",
                    0f,
                    y + with(density) { 4.dp.toPx() },
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = with(density) { 12.dp.toPx() }
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                )
            }

            // Draw lines for alertness and calmness
            reversedEntries.forEachIndexed { index, checkInData ->
                // Calculate the positions
                val nextIndex = (index + 1) % reversedEntries.size
                val x1 = spacePerEntry * (index + 1)
                val y1Alertness = height - (height * (checkInData.alertness) / maxY)
                val y1Calmness = height - (height * (checkInData.calmness) / maxY)
//                val textMargin = 15.dp.toPx() // Space between the chart and the text
//                val density = this.density

                // Points for Alertness
                drawCircle(
                    color = Color.Red, // Use same color but can change for visibility
                    center = Offset(x1, y1Alertness),
                    radius = 16f // Adjust size of the point as needed
                )

                // Points for Calmness
                drawCircle(
                    color = Color.Blue.copy(alpha = 0.7F), // Use same color but can change for visibility
                    center = Offset(x1, y1Calmness),
                    radius = 16f // Adjust size of the point as needed
                )

                if (nextIndex > 0) {
                    val nextEntry = reversedEntries[nextIndex]
                    val x2 = spacePerEntry * (nextIndex + 1)
                    val y2Alertness = height - (height * (nextEntry.alertness) / maxY)
                    val y2Calmness = height - (height * (nextEntry.calmness) / maxY)

                    // Draw line for Alertness
                    drawLine(
                        color = Color.Red,
                        start = Offset(x1, y1Alertness),
                        end = Offset(x2, y2Alertness),
                        strokeWidth = 8f
                    )

                    // Draw line for Calmness
                    drawLine(
                        color = Color.Blue.copy(alpha = 0.7f),
                        start = Offset(x1, y1Calmness),
                        end = Offset(x2, y2Calmness),
                        strokeWidth = 8f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Space between chart and date-time rows

        // Row for Dates
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            reversedEntries.forEach { checkInData ->
                val dateStr = dateFormat.format(checkInData.date.toDate())
                val timeStr = timeFormat.format(checkInData.date.toDate())

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .clickable { onPointClick(checkInData) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dateStr,
                        color = Color.Black,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = timeStr,
                        color = Color.Black,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DetailViewOverlay(
    entry: CheckInData?,
    measuredHeight: Int,
    onDismiss: () -> Unit,
) {
    if (entry != null) {
        // Dimmed background overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { measuredHeight.toDp() })
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onDismiss) // Dismiss on outside click
        ) {
            // Detail view content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(Color.White, shape = RoundedCornerShape(20.dp))
                    .padding(20.dp)
                    .align(Alignment.Center)
                    .clickable { /* Prevent dismissal when tapping inside */ }
            ) {
                Text(
                    text = "Check-in Details",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    if (entry.mood.isNotEmpty()) {
                        val moodEmojiMap = mapOf(
                            "happy" to "😄",
                            "calm" to "☺️",
                            "stressed" to "😣",
                            "bored" to "😐",
                            "tired" to "🥱"
                        )

                        Text(
                            text = "Moods:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        Text(
                            text = entry.mood.split(",").joinToString(", ") {
                                moodEmojiMap[it.trim()] ?: it
                            },
                            modifier = Modifier.padding(bottom = 12.dp), fontSize = 16.sp

                        )
                    }

                    if (entry.moodReason!!.isNotEmpty()) {
                        Text(text = "Reason:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = entry.moodReason,
                            modifier = Modifier.padding(bottom = 12.dp),
                            fontSize = 16.sp
                        )
                    }

                    if (entry.nextAction!!.isNotEmpty()) {
                        Text(text = "Next Action:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = entry.nextAction,
                            modifier = Modifier.padding(bottom = 12.dp),
                            fontSize = 16.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Alertness:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = entry.alertness.toString(), fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Calmness:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = entry.calmness.toString(), fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        .height(45.dp)
                ) {
                    Text(
                        text = "Close",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,

                        )
                }
            }
        }
    }
}
