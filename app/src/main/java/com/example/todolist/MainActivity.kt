package com.example.todolist

import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todolist.ui.theme.TaskDetailScreen
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.random.Random

private var taskList by mutableStateOf(listOf<Task>())

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("ToDoList", MODE_PRIVATE)
        taskList = loadTasks()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Navigation()
            }
        }
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

    private fun saveTasks() {
        val jsonTasks = Gson().toJson(taskList)
        sharedPreferences.edit().apply {
            putString("taskList", jsonTasks)
        }.apply()
    }

    @Composable
    fun Navigation() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Screen.TaskListScreen.route) {
            composable(Screen.TaskListScreen.route) {
                TaskListScreen(navController = navController, tasks = taskList, onDelete = ::deleteTask, onCompleteToggle = ::toggleCompleteTask)
            }

            fun updateTaskTitle(taskId: Int, newTitle: String) {
                val taskIndex = taskList.indexOfFirst { it.id == taskId }
                if (taskIndex != -1) {
                    taskList = taskList.toMutableList().apply {
                        this[taskIndex] = this[taskIndex].copy(title = newTitle)
                    }
                }
            }

            composable("taskDetail/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull()
                val task = taskList.find { it.id == taskId }
                TaskDetailScreen(navController = navController, task = task, taskList = taskList, saveTasks = ::saveTasks, updateTaskTitle = ::updateTaskTitle, toggleCompleteTask = ::toggleCompleteTask)
            }
        }
    }

    @Composable
    fun TaskListScreen(navController: NavHostController, tasks: List<Task>, onDelete: (Int) -> Unit, onCompleteToggle: (Int) -> Unit) {
        var textValue by remember { mutableStateOf("") }
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
                        val newTask = Task(id = Random.nextInt(), textValue.trimEnd { it == '\n' })
                        taskList = taskList + newTask
                        textValue = ""
                        saveTasks()
                    },
                    modifier = Modifier.weight(8f)
                )

                Icon(Icons.Default.Warning, contentDescription = "", modifier = Modifier.padding(start = 8.dp))

                PriorityPicker(modifier = Modifier.weight(2f))

                TextButton (
                    onClick = {},
                    modifier = Modifier
                        .weight(2f)
                ) {
                    Text("Add")
                }
            }
        }
    }

    @Composable
    fun TaskRow(task: Task, onDelete: () -> Unit, onCompleteToggle: () -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier.clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Checkbox(
                checked = task.isComplete,
                onCheckedChange = { onCompleteToggle() }
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(textDecoration = if (task.isComplete) TextDecoration.LineThrough else TextDecoration.None)) {
                        append(task.title)
                    }
                },
                color = if (task.isComplete) Color.Gray else Color.Black
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }

    @Composable
    fun TaskList(tasks: List<Task>, onDelete: (Int) -> Unit, onCompleteToggle: (Int) -> Unit, navController: NavHostController, modifier: Modifier = Modifier) {
        LazyColumn(
            modifier = modifier
                .padding(16.dp)
        ) {
            items(tasks.size) { index ->
                TaskRow(
                    task = tasks[index],
                    onDelete = { onDelete(index) },
                    onCompleteToggle = { onCompleteToggle(index) },
                    onClick = { navController.navigate(Screen.TaskDetailScreen(tasks[index].id).createRoute(tasks[index].id)) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (index < tasks.size - 1) {
                    Divider()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun InputBox(textValue: String, onValueChange: (String) -> Unit, onDone: () -> Unit, modifier: Modifier) {
        TextField(
            value = textValue,
            onValueChange = onValueChange,
            label = { Text("Add new task") },
            modifier = modifier
                .fillMaxWidth()
                .onKeyEvent {
                    if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                        if (textValue.isNotBlank()) {
                            onDone()
                        }
                        true
                    } else {
                        false
                    }
                },
            keyboardActions = KeyboardActions(
                onDone = {
                    if (textValue.isNotBlank()) {
                        onDone()
                    }
                }
            ),
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
        )
    }
    
    @Composable
    fun PriorityPicker(modifier: Modifier) {
        val options = listOf("None", "Low", "Medium", "High")
        var expanded by remember { mutableStateOf(false) }
        var selectedOption by remember { mutableStateOf(options[0]) }

        Box(modifier = modifier.wrapContentSize()) {
            Text(text = selectedOption, modifier = modifier.clickable {expanded = true } )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}