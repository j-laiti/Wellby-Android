package com.rcsi.wellby.toolkitTab.calendar
// display for the week view on the home screen which includes the date and a circle around each
// to indicate the number of to-do list items completed

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rcsi.wellby.toolkitTab.toDoList.ToDoViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeekView(navController: NavController, weekStore: WeekStore = viewModel(), toDoViewModel: ToDoViewModel) {
    val currentWeek by weekStore.currentWeek.collectAsState()
    val scrollState = rememberScrollState()

    Surface(
        elevation = 10.dp,
        modifier = Modifier.padding(horizontal = 10.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.onSecondary
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { weekStore.adjustWeek(-1) }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIosNew,
                        contentDescription = "arrow back",
                        modifier = Modifier.size(MaterialTheme.typography.titleMedium.fontSize.value.dp)
                    )
                }
                // Display the month, assuming all dates fall within the same month for simplicity
                currentWeek?.dates?.firstOrNull()?.let { date ->
                    Text(
                        SimpleDateFormat("MMMM", Locale.getDefault()).format(date),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }


                IconButton(onClick = { weekStore.adjustWeek(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "arrow forward",
                        modifier = Modifier.size(MaterialTheme.typography.titleMedium.fontSize.value.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                currentWeek?.dates?.forEach { date ->
                    val completedCount by toDoViewModel.getCompletedTasksCount(date)
                        .observeAsState(0)
                    val totalCount by toDoViewModel.getTotalTasksCount(date).observeAsState(0)

                    DayView(
                        date,
                        completedCount,
                        totalCount,
                        isToday = isToday(date)
                    ) { selectedDate ->
                        toDoViewModel.setSelectedDate(selectedDate)
                        navController.navigate("toDoScreen")
                    }
                }
            }
        }
    }
}

@Composable
fun DayView(
    date: Date,
    completedTaskCount: Int,
    totalTaskCount: Int,
    isToday: Boolean,
    onDateSelected: (Date) -> Unit)
{
    val dateFormatter = remember { SimpleDateFormat("d", Locale.getDefault()) }
    val dayOfMonth = dateFormatter.format(date)
    val dayOfWeek = SimpleDateFormat("E", Locale.getDefault()).format(date).take(1)

    val completionRatio = if (totalTaskCount > 0) completedTaskCount.toFloat() / totalTaskCount else 0f
    val primaryColor = MaterialTheme.colorScheme.primary

    val textStyle = if (isToday) {
        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    } else {
        MaterialTheme.typography.titleMedium
    }

    Column(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .clickable { onDateSelected(date) }
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayOfWeek,
            style = textStyle
        )

        if (totalTaskCount > 0) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(36.dp), onDraw = {
                    drawCircle(
                        color = Color.LightGray,
                        radius = size.minDimension / 2,
                        style = Stroke(width = 8f)
                    )

                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 360 * completionRatio,
                        useCenter = false,
                        size = size,
                        style = Stroke(width = 8f)
                    )

                })
                Text(text = dayOfMonth, style = textStyle)
            }
        } else {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(36.dp), onDraw = {
                    //clear circle so the days are aligned
                    drawCircle(
                        color = Color.Transparent,
                        radius = size.minDimension / 2,
                        style = Stroke(width = 8f)
                    )
                })
                Text(text = dayOfMonth, style = textStyle)
            }
        }

    }
}

fun isToday(date: Date): Boolean {
    val currentDate = Calendar.getInstance().time
    return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date) ==
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentDate)
}
