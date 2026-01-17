package com.example.todolist.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String, // In production, store hashed password
    val email: String = "",
    val isLoggedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// Login response from API
data class LoginResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val token: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

