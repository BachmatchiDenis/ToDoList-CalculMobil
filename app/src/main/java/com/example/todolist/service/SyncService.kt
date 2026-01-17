package com.example.todolist.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.example.todolist.TodoApplication
import com.example.todolist.receiver.NotificationReceiver
import kotlinx.coroutines.*

class SyncService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startPeriodicSync()
        scheduleDeadlineNotifications()
        return START_STICKY
    }

    private fun startPeriodicSync() {
        syncJob?.cancel()
        syncJob = serviceScope.launch {
            while (isActive) {
                try {
                    val app = application as TodoApplication
                    app.taskRepository.syncUnsyncedTasks()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(5 * 60 * 1000) // Sync every 5 minutes
            }
        }
    }

    private fun scheduleDeadlineNotifications() {
        serviceScope.launch {
            try {
                val app = application as TodoApplication
                val tasks = app.taskRepository.getTasksWithUpcomingDeadlines()

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                for (task in tasks) {
                    task.deadline?.let { deadline ->
                        // Schedule notification 30 minutes before deadline
                        val notificationTime = deadline - (30 * 60 * 1000)

                        if (notificationTime > System.currentTimeMillis()) {
                            val intent = Intent(this@SyncService, NotificationReceiver::class.java).apply {
                                putExtra(NotificationReceiver.EXTRA_TASK_ID, task.id)
                                putExtra(NotificationReceiver.EXTRA_TASK_TITLE, task.title)
                            }

                            val pendingIntent = PendingIntent.getBroadcast(
                                this@SyncService,
                                task.id.toInt(),
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (alarmManager.canScheduleExactAlarms()) {
                                    alarmManager.setExactAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        notificationTime,
                                        pendingIntent
                                    )
                                }
                            } else {
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    notificationTime,
                                    pendingIntent
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

