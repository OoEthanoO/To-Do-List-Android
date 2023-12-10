package com.example.todolist

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    navController: NavHostController,
    task: Task?,
    saveTasks: () -> Unit,
    updateTaskTitle: (Int, String) -> Unit,
    toggleCompleteTask: (Int) -> Unit,
    toggleHaveDueDate: (Int) -> Unit,
    updateDueDate: (Int, Long) -> Unit
) {
    var taskTitle by remember { mutableStateOf(task?.title ?: "") }
    val initialDate = if (task?.haveDueDate == true) {
        Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
    } else {
        LocalDate.now()
    }
    val selectedDate = remember { mutableStateOf(initialDate) }
    val context = LocalContext.current
    val zoneId = ZoneId.systemDefault()

    fun showDatePicker() {
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            selectedDate.value = LocalDate.of(year, month + 1, dayOfMonth)
            if (task != null) {
                updateDueDate(task.id, selectedDate.value.atStartOfDay(zoneId).toEpochSecond() * 1000)
            }
        }, selectedDate.value.year, selectedDate.value.monthValue - 1, selectedDate.value.dayOfMonth).show()
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                saveTasks()
                navController.popBackStack()
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
            }
            Text("To Do List", style = TextStyle(fontSize = 18.sp))
        }
        Text(
            "Edit Task",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp)
        )
        if (task != null) {
            TextField(
                value = taskTitle,
                onValueChange = { newTitle ->
                    if (newTitle.isNotBlank()) {
                        taskTitle = newTitle
                        updateTaskTitle(task.id, newTitle)
                        saveTasks()
                    }
                },
                modifier = Modifier
                    .padding(start = 34.dp, end = 50.dp, top = 50.dp)
                    .fillMaxWidth(),
                label = { Text("Task Title") },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
            )

            Row (modifier = Modifier
                .padding(start = 50.dp, end = 50.dp, top = 25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mark Complete", modifier = Modifier.weight(1f))
                Checkbox(
                    checked = task.isComplete,
                    onCheckedChange = {
                        toggleCompleteTask(task.id)
                    }
                )
            }

            Row(modifier = Modifier
                .padding(start = 50.dp, end = 50.dp, top = 25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Priority", modifier = Modifier.weight(1f))
                PriorityPicker(modifier = Modifier.padding(end = 4.dp), onPriorityChange = { newPriority: String ->
                    task.priority = newPriority
                    saveTasks()
                }, startingOption = task.priority)
            }

            Row(modifier = Modifier
                .padding(start = 50.dp, end = 50.dp, top = 25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Due Date", modifier = Modifier.weight(1f))
                if (task.haveDueDate) {
                    val date =
                        task.dueDate.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                    val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
                    val formattedDate = date?.format(formatter)
                    if (formattedDate != null) {
                        Box(
                            modifier = Modifier
                                .wrapContentSize()
                                .weight(3f)
                        ) {
                            Text(
                                text = formattedDate,
                                modifier = Modifier
                                    .clickable { showDatePicker() }
                            )
                        }
                    }
                }
                Checkbox(
                    checked = task.haveDueDate,
                    onCheckedChange = { isChecked ->
                        toggleHaveDueDate(task.id)
                        if (isChecked) {
                            showDatePicker()
                        }
                    }
                )
            }
        }
    }
}