package com.example.todolist

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class CheckTasksWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        Log.i("CheckTasksWorker", "Checking tasks")
        val tasks = loadTasks()

        val overdueTasks = tasks.filter { it.haveDueDate &&  it.isOverdue() && !it.isComplete }
        val dueTodayTasks = tasks.filter { it.haveDueDate && it.isDueToday() && !it.isComplete }

        if (overdueTasks.isNotEmpty()) {
            Log.i("CheckTasksWorker", "Overdue tasks: ${overdueTasks.size}")
            sendNotification("Overdue Tasks", "You have ${overdueTasks.size} overdue tasks")
        }

        if (dueTodayTasks.isNotEmpty()) {
            Log.i("CheckTasksWorker", "Due today tasks: ${dueTodayTasks.size}")
            sendNotification("Due Today Tasks", "You have ${dueTodayTasks.size} tasks due today")
        }

        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("task_due_channel", "Task Due", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "task_due_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }
}