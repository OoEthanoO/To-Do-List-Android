package com.example.todolist

import android.content.*
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.random.Random

private var taskList by mutableStateOf(listOf<Task>())
lateinit var sharedPreferences: SharedPreferences

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("ToDoList", MODE_PRIVATE)
        taskList = loadTasks()

        for (task in taskList) {
            if (task.priority == null) {
                task.priority = "None"
            }
            if (task.createdAt == 0L) {
                task.createdAt = System.currentTimeMillis()
            }
            saveTasks()
        }

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

    private fun deleteTask(taskId: Int) {
        taskList = taskList.filter { it.id != taskId }
        saveTasks()
    }

    private fun toggleCompleteTask(taskId: Int) {
        taskList = taskList.map { task ->
            if (task.id == taskId) {
                task.copy(isComplete = !task.isComplete)
            } else {
                task
            }
        }
        saveTasks()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun Navigation() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Screen.TaskListScreen.route) {
            composable(Screen.TaskListScreen.route) {
                TaskListScreen(navController = navController, tasks = taskList, onDelete = ::deleteTask, onCompleteToggle = ::toggleCompleteTask, sharedPreferences)
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
                TaskDetailScreen(
                    navController = navController,
                    task = task,
                    saveTasks = ::saveTasks,
                    updateTaskTitle = ::updateTaskTitle,
                    toggleCompleteTask = ::toggleCompleteTask
                )
            }
        }
    }
}

@Composable
fun PriorityPicker(modifier: Modifier, onPriorityChange: (String) -> Unit, startingOption: String = "None") {
    val options = listOf("None", "Low", "Medium", "High")
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(startingOption) }

    Box(modifier = modifier.wrapContentSize()) {
        Text(text = selectedOption, modifier = modifier.clickable {expanded = true } )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        offset = DpOffset(x = LocalConfiguration.current.screenWidthDp.dp, y = 0.dp)
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                    selectedOption = option
                    onPriorityChange(option)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun SortByPicker(modifier: Modifier, onPriorityChange: (String) -> Unit, startingOption: String = "Creation Date") {
    val options = listOf("Creation Date", "Completion", "Priority", "Title")
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(startingOption) }

    Box(modifier = modifier.wrapContentSize()) {
        Text(text = selectedOption, modifier = modifier.clickable {expanded = true } )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                    selectedOption = option
                    onPriorityChange(option)
                    expanded = false
                }
            )
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
                onDelete = { onDelete(tasks[index].id) },
                onCompleteToggle = { onCompleteToggle(tasks[index].id) },
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

fun addTask(textValue: String, priority: String) {
    val actualTextValue = textValue.trimEnd { it == '\n' }
    if (actualTextValue.isBlank()) {
        return
    }
    val newTask = Task(id = Random.nextInt(), textValue.trimEnd { it == '\n' }, priority = priority, createdAt = (System.currentTimeMillis() + 7))
    taskList = taskList + newTask
    saveTasks()
}

private fun saveTasks() {
    val jsonTasks = Gson().toJson(taskList)
    sharedPreferences.edit().apply {
        putString("taskList", jsonTasks)
    }.apply()
}