package com.rcsi.wellby.toolkitTab.toDoList
// screen which displays to-do items within each date in the calendar view on the home tab
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rcsi.wellby.signinSystem.AuthManager
import java.text.SimpleDateFormat
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoScreen(viewModel: ToDoViewModel, userManager: AuthManager) {

    val toDoItems by viewModel.toDos.observeAsState(initial = emptyList())
    val selectedDate by viewModel.selectedDate.observeAsState()
    var textState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        userManager.viewDidAppear("ToDoList")
    }

    Column(
        modifier = Modifier.padding(15.dp)
    ) {
        Text(
            text = "To-do List",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        selectedDate?.let {
            val formattedDate =
                SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(it)
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
        }

        TextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text("Add a new to-do") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    selectedDate?.let { date ->
                        viewModel.addToDoItem(textState, date)
                        textState = ""
                    }
                    userManager.clickedOn("new todo created")
                }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                }
            }
        )

        LazyColumn {
            items(items = toDoItems, key = { item -> item.id }) { item ->
                var itemDismissed by remember { mutableStateOf(false) }

                if (!itemDismissed) {
                    SwipeToDismiss(
                        state = rememberDismissState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == DismissValue.DismissedToEnd || dismissValue == DismissValue.DismissedToStart) {
                                    viewModel.deleteToDoItem(item)
                                    itemDismissed =
                                        true // This is to prevent the item from being immediately recomposed back into the list.
                                    true
                                } else {
                                    false
                                }
                            }
                        ),
                        directions = setOf(
                            DismissDirection.StartToEnd,
                            DismissDirection.EndToStart
                        ),
                        background = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        },
                        dismissContent = {
                            ToDoItem(
                                toDoItem = item,
                                onToDoClick = {
                                    viewModel.toggleToDoComplete(item)
                                    userManager.clickedOn("todo completed")
                                }
                            )
                        }
                    )

                    Divider()
                }

            }
        }
    }
}


@Composable
fun ToDoItem(toDoItem: ToDoItemEntity, onToDoClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onToDoClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (toDoItem.isComplete) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (toDoItem.isComplete) "Completed Task" else "Incomplete Task",
            tint = if (toDoItem.isComplete) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = toDoItem.title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = if (toDoItem.isComplete) Color.Gray else Color.Black
        )
    }
}

