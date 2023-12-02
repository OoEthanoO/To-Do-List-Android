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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.random.Random

data class Task(
    val id: Int,
    val title: String,
    val isComplete: Boolean = false
)

private var taskList by mutableStateOf(listOf<Task>())

sealed class Screen(val route: String) {
    object TaskListScreen : Screen("task_list")
    data class TaskDetailScreen(val taskId: Int) : Screen("taskDetail/{taskId}") {
        fun createRoute(taskId: Int): String {
            return "taskDetail/$taskId"
        }
    }
}

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
            composable("taskDetail/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull()
                val task = taskList.find { it.id == taskId }
                TaskDetailScreen(navController = navController, task = task)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
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

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
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

            TextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("Add new task") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onKeyEvent { it ->
                        if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                            if (textValue.isNotBlank()) {
                                val newTask = Task(id = Random.nextInt(), textValue.trimEnd { it == '\n' })
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
                            val newTask = Task(id = Random.nextInt(), textValue)
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

    @Composable
    fun TaskDetailScreen(navController: NavHostController, task: Task?) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
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
                Text(
                    task.title,
                    modifier = Modifier.padding(50.dp),
                    style = TextStyle(fontSize = 18.sp)
                )
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
}