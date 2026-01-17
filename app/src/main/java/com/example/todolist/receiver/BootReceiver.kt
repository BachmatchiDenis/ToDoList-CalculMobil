package com.example.todolist.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todolist.service.SyncService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Restart sync service after device boot
            val serviceIntent = Intent(context, SyncService::class.java)
            context.startService(serviceIntent)
        }
    }
}

