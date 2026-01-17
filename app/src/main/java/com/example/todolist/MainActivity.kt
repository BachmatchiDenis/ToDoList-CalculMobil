// This file is deprecated - use com.example.todolist.ui.MainActivity instead
// Keeping for backwards compatibility
package com.example.todolist

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.ui.LoginActivity

/**
 * Redirect Activity - just redirects to LoginActivity
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
