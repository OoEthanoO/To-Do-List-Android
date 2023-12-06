package com.example.todolist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun TaskListScreen(navController: NavHostController, tasks: List<Task>, onDelete: (Int) -> Unit, onCompleteToggle: (Int) -> Unit) {
    var textValue by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf("None") }
    Column(
        modifier = Modifier.fillMaxSize())
    {
        Text(
            text = "To Do List",
            modifier = Modifier.padding(16.dp),
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 36.sp)
        )

        TaskList(tasks = tasks, onDelete = onDelete, onCompleteToggle = onCompleteToggle, navController = navController, modifier = Modifier.weight(1f))

        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            InputBox(
                textValue = textValue,
                onValueChange = { textValue = it },
                onDone = {
                    addTask(textValue, selectedOption)
                    textValue = ""
                },
                modifier = Modifier.weight(8f)
            )

            Icon(
                Icons.Default.Warning,
                contentDescription = "",
                modifier = Modifier
                    .padding(start = 8.dp),
                tint = when (selectedOption) {
                    "None" -> Color.Gray
                    "Low" -> Color.Green
                    "Medium" -> Color.Yellow
                    "High" -> Color.Red
                    else -> Color.Transparent
                }
            )

            PriorityPicker(modifier = Modifier.weight(2f)) { newPriority ->
                selectedOption = newPriority
            }

            TextButton (
                onClick = {
                    addTask(textValue, selectedOption)
                    textValue = ""
                },
                modifier = Modifier
                    .weight(2f)
            ) {
                Text("Add")
            }
        }
    }
}