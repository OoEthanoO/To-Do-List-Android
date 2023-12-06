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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
private lateinit var sharedPreferences: SharedPreferences

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("ToDoList", MODE_PRIVATE)
        taskList = loadTasks()

        for (task in taskList) {
            if (task.priority == null) {
                task.priority = "None"
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
}

@Composable
fun PriorityPicker(modifier: Modifier, onPriorityChange: (String) -> Unit) {
    val options = listOf("None", "Low", "Medium", "High")
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(options[0]) }

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
        if (task.priority != "None") {
            Icon(
                Icons.Default.Warning,
                contentDescription = "",
                tint = when (task.priority) {
                    "Low" -> Color.Green
                    "Medium" -> Color.Yellow
                    else -> Color.Red
                },
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(textDecoration = if (task.isComplete) TextDecoration.LineThrough else TextDecoration.None)) {
                    append(task.title)
                }
            },
            color =
            if (task.isComplete) {
                Color.Gray
            } else {
                when (task.priority) {
                    "Low" -> {
                        Color.Green
                    }
                    "Medium" -> {
                        Color(0xFFffe000)
                    }
                    "High" -> {
                        Color.Red
                    }
                    else -> {
                        Color.Black
                    }
                }
            },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
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
    val newTask = Task(id = Random.nextInt(), textValue.trimEnd { it == '\n' }, priority = priority)
    taskList = taskList + newTask
    saveTasks()
}

private fun saveTasks() {
    val jsonTasks = Gson().toJson(taskList)
    sharedPreferences.edit().apply {
        putString("taskList", jsonTasks)
    }.apply()
}