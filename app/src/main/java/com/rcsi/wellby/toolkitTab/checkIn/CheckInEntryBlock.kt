package com.rcsi.wellby.toolkitTab.checkIn
// view for the check-in block displayed on the home screen which includes a field for mood, alertness and relaxed


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInEntryBlock(
    userId: String,
    navController: NavController,
    mood: String,
    onMoodChange: (String) -> Unit,
    alertSelected: Int,
    onAlertChange: (Int) -> Unit,
    calmSelected: Int,
    onCalmChange: (Int) -> Unit,
    isHomeScreen: Boolean = true,
    onSubmit: () -> Unit
) {
    val checkInManager = viewModel<CheckInManager>()

    var alertMenuExpanded by remember { mutableStateOf(false) }
    var calmMenuExpanded by remember { mutableStateOf(false) }
    var isFormValid = mood.isNotEmpty() && alertSelected != 0 && calmSelected != 0

    var isTextFieldFocused by remember { mutableStateOf(false) }

    var showInfoDialog by remember { mutableStateOf(false) }

    val customReason = remember { mutableStateOf("") }
    val customAction = remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val showExtendedCheckIn = remember { mutableStateOf(false) }


    Surface(
        elevation = 10.dp,
        modifier = Modifier.padding(10.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.onSecondary,
    ) {
        Column {
            // Title and information button
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "I'm feeling:",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 15.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (isHomeScreen) {
                    IconButton(onClick = {
                        showInfoDialog = true
                    }) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                if (isHomeScreen) {
                    IconButton(onClick = {
                        navController.navigate("checkinTracker")
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Navigate",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (showInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showInfoDialog = false },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = {
                            showInfoDialog = false
                        }) {
                            androidx.compose.material3.Text("Close")
                        }
                    },
                    title = { Text("How to Check-In") },
                    text = { androidx.compose.material3.Text("Enter a single emoji that represents your current mood. Then, select your level of alertness and calmness from 1-5, where 5 represents the most alert and calm.") }
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                val inputColumnHeight = 90.dp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .height(inputColumnHeight)
                        .weight(1f)
                ) {
                    MoodTextField(
                        mood = mood,
                        onMoodChange = onMoodChange,
                        isTextFieldFocused = isTextFieldFocused,
                        onTextFieldFocusChange = { isTextFieldFocused = it }
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Mood",
                        Modifier.padding(top = 4.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .height(inputColumnHeight)
                        .weight(1f)
                ) {
                    Spacer(Modifier.weight(1f))
                    SelectionDropdown(
                        selectedValue = alertSelected,
                        onValueChange = onAlertChange,
                        menuExpanded = alertMenuExpanded,
                        onMenuChange = { alertMenuExpanded = it }
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Alert",
                        Modifier.padding(top = 4.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .height(inputColumnHeight)
                        .weight(1f)
                ) {
                    Spacer(Modifier.weight(1f))
                    SelectionDropdown(
                        selectedValue = calmSelected,
                        onValueChange = onCalmChange,
                        menuExpanded = calmMenuExpanded,
                        onMenuChange = { calmMenuExpanded = it }
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Relaxed",
                        Modifier.padding(top = 4.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (isHomeScreen) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .height(inputColumnHeight)
                            .weight(0.5f)
                    ) {

                        TextButton(
                            onClick = {
                                checkInManager.saveCheckInData(
                                    CheckInData(
                                        mood,
                                        alertSelected,
                                        calmSelected
                                    ), userId
                                )
                                onSubmit()
                            },
                            enabled = isFormValid,
                        ) {
                            Icon(
                                Icons.Filled.AddCircle,
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = {
                            coroutineScope.launch {
                                sheetState.show()
                                showExtendedCheckIn.value = true
                            }
                        }, Modifier.padding(top = 4.dp, bottom = 4.dp)) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Navigate",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExtendedCheckIn.value) {
        ModalBottomSheet(
            onDismissRequest = {
                showExtendedCheckIn.value = false
            },
            sheetState = sheetState
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Sheet content
                ExtendedCheckIn(
                    checkInManager = checkInManager,
                    userId = userId,
                    mood = remember { mutableStateOf(mood) },  // Pass the actual mood here
                    relaxedSlider = remember { mutableStateOf(calmSelected.toFloat()) },  // Pass the calmSelected slider value
                    alertSlider = remember { mutableStateOf(alertSelected.toFloat()) },  // Pass the alertSelected slider value
                    customReason = customReason,  // Pass the customReason state
                    customAction = customAction,  // Pass the customAction state
                    showExtendedCheckIn = showExtendedCheckIn,
                )

                // Close Icon at the Top Right
                IconButton(
                    onClick = {
                        if (sheetState.isVisible) {
                            showExtendedCheckIn.value = false
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun MoodTextField(
    mood: String,
    onMoodChange: (String) -> Unit,
    isTextFieldFocused: Boolean,
    onTextFieldFocusChange: (Boolean) -> Unit
) {
    TextField(
        value = mood,
        onValueChange = { if (it.length <= 2) onMoodChange(it) },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 24.sp),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = Modifier
            .width(70.dp)
            .onFocusChanged { focusState ->
                onTextFieldFocusChange(focusState.isFocused)
            },
        label = {
            if (!isTextFieldFocused) Text("  \uD83D\uDE42") // Use your emoji or label here
        }
    )
}


@Composable
fun SelectionDropdown(
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    menuExpanded: Boolean,
    onMenuChange: (Boolean) -> Unit,
    range: IntRange = 1..5
) {
    Box(modifier = Modifier.wrapContentSize()) {
        TextButton(onClick = { onMenuChange(true) }) {
            Text(
                if (selectedValue == 0) "_" else selectedValue.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                color = MaterialTheme.colorScheme.primary
            )
            Icon(Icons.Filled.ArrowDropDown, "dropdown", tint = MaterialTheme.colorScheme.primary)
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { onMenuChange(false) }
        ) {
            // Generate menu items for the specified range (1-5)
            range.forEach { index ->
                DropdownMenuItem(onClick = {
                    onValueChange(index)
                    onMenuChange(false)
                }) {
                    Text(
                        text = index.toString()
                    )
                }
            }
        }
    }
}


@Composable
fun ExtendedCheckIn(
    checkInManager: CheckInManager,
    userId: String,
    mood: MutableState<String>,
    relaxedSlider: MutableState<Float>,
    alertSlider: MutableState<Float>,
    customReason: MutableState<String>,
    customAction: MutableState<String>,
    showExtendedCheckIn: MutableState<Boolean>
) {
    val selectedMood = remember { mutableStateListOf<String>() }
    val selectedReason = remember { mutableStateListOf<String>() }
    val selectedAction = remember { mutableStateListOf<String>() }

    val isFormValid = mood.value.isNotEmpty() || selectedMood.isNotEmpty()

    val emojiItems = listOf(
        "happy" to "😊",
        "calm" to "☺️",
        "stressed" to "😣",
        "bored" to "😐",
        "tired" to "🥱"
    )

    val moodItems = listOf(
        "school" to "📚",
        "social life" to "🗣️",
        "family" to "🏠",
        "sleep" to "🛌"
    )

    val likeNextItems = listOf(
        "journal" to "✍️",
        "breathe" to "😮‍💨",
        "HRV" to "♥️",
        "chat" to "💬"
    )

    val othersItems = listOf(
        "talk with someone" to "🫂",
        "take a break" to "🚦",
        "get outside" to "⛰️",
        "move your body" to "🚶🏻‍♀️‍➡️"
    )

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "How are you feeling today?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 50.dp)
        )

        // Emoji Buttons Row
        LazyRow(
            modifier = Modifier
                .padding(8.dp)
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(emojiItems.size) { index ->
                val item = emojiItems[index]
                val isSelected = selectedMood.contains(item.first)

                EmojiCard(
                    label = item.first,
                    emoji = item.second,
                    isSelected = isSelected,
                    isFixedWidth = true,
                    onClick = {
                        if (isSelected) {
                            selectedMood.remove(item.first)
                        } else {
                            selectedMood.add(item.first)
                        }
                    }
                )
            }
        }

        // Mood TextField
        Card(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(0.4f)
                .height(50.dp)
                .background(
                    Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(10.dp)
                ), // Light background
            shape = RoundedCornerShape(10.dp),
            elevation = 0.dp // No elevation for flat appearance
        ) {
            TextField(
                value = mood.value,
                onValueChange = { mood.value = it },
                label = { Text("🫥 Other") },
                modifier = Modifier
                    .fillMaxSize(),

                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Gray,
                    disabledTextColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        }

        // Relaxed Slider
        Text(
            text = "How relaxed are you feeling? 🧘🏽‍♂️", fontSize = 20.sp, modifier = Modifier
                .padding(top = 20.dp)
        )
        Slider(
            value = relaxedSlider.value,
            onValueChange = { relaxedSlider.value = it },
            valueRange = 1f..5f,
            steps = 3,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp)
        )

        // Alert Slider
        Text(
            text = "How alert are you feeling? 😳", fontSize = 20.sp, modifier = Modifier
                .padding(top = 20.dp)
        )
        Slider(
            value = alertSlider.value,
            onValueChange = { alertSlider.value = it },
            valueRange = 1f..5f,
            steps = 3,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp)
        )

        Text(
            text = "What's affecting your mood?",
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 20.dp)
        )

        LazyRow(
            modifier = Modifier
                .padding(8.dp)
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(moodItems.size) { index ->
                val item = moodItems[index]
                val isSelected = selectedReason.contains(item.first)

                EmojiCard(
                    label = item.first,
                    emoji = item.second,
                    isSelected = isSelected,
                    isFixedWidth = false,
                    onClick = {
                        if (isSelected) {
                            selectedReason.remove(item.first)
                        } else {
                            selectedReason.add(item.first)
                        }
                    }
                )
            }
        }

        Card(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(0.4f)
                .height(50.dp)
                .background(
                    Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(10.dp)
                ), // Light background
            shape = RoundedCornerShape(10.dp),
            elevation = 0.dp // No elevation for flat appearance
        ) {
            TextField(
                value = customReason.value,
                onValueChange = { customReason.value = it },
                label = { Text("Other") },
                modifier = Modifier
                    .fillMaxSize(),

                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Gray,
                    disabledTextColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        }

        Text(
            text = "What would you like to do next?",
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 20.dp)
        )

        Text(
            text = "In-App Actions",
            fontSize = 16.sp,
            modifier = Modifier
                .padding(top = 10.dp)
                .alpha(ContentAlpha.medium),
            color = Color.Gray
        )

        LazyRow(
            modifier = Modifier
                .padding(8.dp)
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(likeNextItems.size) { index ->
                val item = likeNextItems[index]
                val isSelected = selectedAction.contains(item.first)

                EmojiCard(
                    label = item.first,
                    emoji = item.second,
                    isSelected = isSelected,
                    isFixedWidth = false,
                    onClick = {
                        if (isSelected) {
                            selectedAction.remove(item.first)
                        } else {
                            selectedAction.add(item.first)
                        }
                    }
                )
            }
        }

        Text(
            text = "Other Actions",
            fontSize = 16.sp,
            modifier = Modifier
                .padding(top = 10.dp)
                .alpha(ContentAlpha.medium),
            color = Color.Gray
        )

        LazyRow(
            modifier = Modifier
                .padding(8.dp)
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(othersItems.size) { index ->
                val item = othersItems[index]
                val isSelected = selectedAction.contains(item.first)

                EmojiCard(
                    label = item.first,
                    emoji = item.second,
                    isSelected = isSelected,
                    isFixedWidth = false,
                    onClick = {
                        if (isSelected) {
                            selectedAction.remove(item.first)
                        } else {
                            selectedAction.add(item.first)
                        }
                    }
                )
            }
        }

        Card(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(0.4f)
                .height(50.dp)
                .background(
                    Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(10.dp)
                ), // Light background
            shape = RoundedCornerShape(10.dp),
            elevation = 0.dp // No elevation for flat appearance
        ) {
            TextField(
                value = customAction.value,
                onValueChange = { customAction.value = it },
                label = { Text("Other") },
                modifier = Modifier
                    .fillMaxSize(),

                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Gray,
                    disabledTextColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        }

        Button(
            onClick = {
                val moodString = selectedMood.joinToString(", ")
                val reasonString = selectedReason.joinToString(", ")
                val actionString = selectedAction.joinToString(", ")

                val moodSelected =
                    if (mood.value.isEmpty()) moodString else moodString + ", " + mood.value
                val reasonSelected =
                    if (customReason.value.isEmpty()) reasonString else reasonString + ", " + customReason.value
                val actionSelected =
                    if (customAction.value.isEmpty()) actionString else actionString + ", " + customAction.value

                // Save check-in data to Firebase
                userId.let { userId ->
                    checkInManager.saveCheckInData(
                        CheckInData(
                            mood = moodSelected,
                            alertness = alertSlider.value.toInt(),
                            calmness = relaxedSlider.value.toInt(),
                            moodReason = reasonSelected,
                            nextAction = actionSelected,
                            date = Timestamp.now()
                        ),
                        userId = userId
                    )
                }

                // Reset values after saving
                selectedMood.clear()
                selectedReason.clear()
                selectedAction.clear()
                mood.value = ""
                alertSlider.value = 1f
                relaxedSlider.value = 1f
                customReason.value = ""
                customAction.value = ""
                showExtendedCheckIn.value = false
            },
            modifier = Modifier
                .padding(top = 16.dp, bottom = 20.dp)
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .height(50.dp),

            enabled = isFormValid,
        ) {
            Text("Finish Check-in", fontSize = 16.sp)
        }
    }
}

@Composable
fun EmojiCard(
    label: String,
    emoji: String,
    isSelected: Boolean,
    isFixedWidth: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .wrapContentHeight() // Wrap content for dynamic height
            .width(if (isFixedWidth) 70.dp else 80.dp) // Fixed width
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(
                color = if (isSelected) Color(0xFFD0E8FF) else Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF4A90E2)) else null,
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp)) // Adjust spacing as needed
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}




