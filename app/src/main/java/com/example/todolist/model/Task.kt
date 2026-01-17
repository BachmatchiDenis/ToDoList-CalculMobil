package com.example.todolist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @SerializedName("todo")
    val title: String,

    val description: String = "",

    @SerializedName("completed")
    val isCompleted: Boolean = false,

    val deadline: Long? = null, // timestamp

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis(),

    val userId: Long = 1,

    val remoteId: Long? = null, // ID from API

    val isSynced: Boolean = false
)

// Response from dummyjson.com/todos
data class TodosResponse(
    val todos: List<ApiTodo>,
    val total: Int,
    val skip: Int,
    val limit: Int
)

data class ApiTodo(
    val id: Long,
    val todo: String,
    val completed: Boolean,
    val userId: Long
)

// For creating/updating tasks
data class TaskRequest(
    val todo: String,
    val completed: Boolean,
    val userId: Long
)

