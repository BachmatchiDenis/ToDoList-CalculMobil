package com.example.todolist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.todolist.data.local.AppDatabase
import com.example.todolist.data.remote.RetrofitClient
import com.example.todolist.data.repository.TaskRepository
import com.example.todolist.data.repository.UserRepository
import com.example.todolist.worker.SyncWorker

class TodoApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    val taskRepository by lazy {
        TaskRepository(
            database.taskDao(),
            RetrofitClient.todoApiService
        )
    }

    val userRepository by lazy {
        UserRepository(
            database.userDao(),
            RetrofitClient.authApiService
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleSyncWorker()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for task deadlines"
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleSyncWorker() {
        SyncWorker.schedule(this)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "task_reminders"
    }
}

