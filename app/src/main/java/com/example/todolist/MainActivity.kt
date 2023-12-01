package com.example.todolist

import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todolist.ui.theme.ToDoListTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.view.WindowCompat

data class Task(
    val title: String,
    val isComplete: Boolean = false
)

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var taskList by mutableStateOf(listOf<Task>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        sharedPreferences = getSharedPreferences("ToDoList", MODE_PRIVATE)
        taskList = loadTasks()

        setContent {
            ToDoListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ToDoContent()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ToDoContent(modifier: Modifier = Modifier) {
        var textValue by remember { mutableStateOf("") }

        Column(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                items(taskList.size) { index ->
                    TaskRow(
                        task = taskList[index],
                        onDelete = { deleteTask(index) },
                        onCompleteToggle = { toggleCompleteTask(index) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (index < taskList.size - 1) {
                        Divider()
                    }
                }
            }

            TextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("Add new task") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onKeyEvent { it ->
                        if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                            if (textValue.isNotBlank()) {
                                val newTask = Task(textValue.trimEnd { it == '\n' })
                                taskList = taskList + newTask
                                textValue = ""
                                saveTasks()
                            }
                            true
                        } else {
                            false
                        }
                    },
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (textValue.isNotBlank()) {
                            val newTask = Task(textValue)
                            taskList = taskList + newTask
                            textValue = ""
                            saveTasks()
                        }
                    }
                ),
                singleLine = true
            )
        }
    }

    private fun saveTasks() {
        val jsonTasks = Gson().toJson(taskList)
        sharedPreferences.edit().apply {
            putString("taskList", jsonTasks)
        }.apply()
    }

    private fun loadTasks(): List<Task> {
        val jsonTasks = sharedPreferences.getString("taskList", null)
        return if (jsonTasks != null) {
            val type = object : TypeToken<List<Task>>() {}.type
            Gson().fromJson(jsonTasks, type)
        } else {
            emptyList()
        }
    }

    private fun deleteTask(index: Int) {
        taskList = taskList.toMutableList().apply { removeAt(index) }
        saveTasks()
    }

    private fun toggleCompleteTask(index: Int) {
        taskList = taskList.toMutableList().apply {
            this[index] = this[index].copy(isComplete = !this[index].isComplete)
        }
        saveTasks()
    }
}

@Composable
fun TaskRow(task: Task, onDelete: () -> Unit, onCompleteToggle: () -> Unit, modifier: Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onCompleteToggle) {
            if (task.isComplete) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Complete", tint = Color.Green)
            } else {
                Icon(Icons.Default.Check, contentDescription = "Incomplete", tint = Color.Blue)
            }
        }
        Text(
            text = buildAnnotatedString { 
                withStyle(style = SpanStyle(textDecoration = if (task.isComplete) TextDecoration.LineThrough else TextDecoration.None)) {
                    append(task.title)
                }
            },
            color = if (task.isComplete) Color.Gray else Color.Black
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ToDoListTheme {
        MainActivity().ToDoContent()
    }
}