package com.example.todolist

data class Task(
    val id: Int,
    val title: String,
    var isComplete: Boolean = false,
    var priority: String = "None",
    var createdAt: Long,
    var haveDueDate: Boolean = false,
    var dueDate: Long = 0
)