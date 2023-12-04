package com.example.todolist

sealed class Screen(val route: String) {
    object TaskListScreen : Screen("task_list")
    data class TaskDetailScreen(val taskId: Int) : Screen("taskDetail/{taskId}") {
        fun createRoute(taskId: Int): String {
            return "taskDetail/$taskId"
        }
    }
}