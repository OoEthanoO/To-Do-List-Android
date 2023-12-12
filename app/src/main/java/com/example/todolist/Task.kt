package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class Task(
    val id: Int,
    val title: String,
    var isComplete: Boolean = false,
    var priority: String = "None",
    var createdAt: Long,
    var haveDueDate: Boolean = false,
    var dueDate: Long = 0
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun isOverdue(): Boolean {
        val date = dueDate.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
        return date.isBefore(LocalDate.now())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isDueToday(): Boolean {
        val date = dueDate.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
        return date.isEqual(LocalDate.now())
    }
}