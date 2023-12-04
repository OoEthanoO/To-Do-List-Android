package com.example.todolist

data class Task(
    val id: Int,
    val title: String,
    var isComplete: Boolean = false
)