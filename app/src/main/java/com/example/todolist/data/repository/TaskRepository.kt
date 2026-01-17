package com.example.todolist.data.repository

import com.example.todolist.data.local.TaskDao
import com.example.todolist.data.remote.TodoApiService
import com.example.todolist.model.Task
import com.example.todolist.model.TaskRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TaskRepository(
    private val taskDao: TaskDao,
    private val apiService: TodoApiService
) {

    fun getAllTasks(userId: Long): Flow<List<Task>> = taskDao.getAllTasks(userId)

    fun getPendingTasks(userId: Long): Flow<List<Task>> = taskDao.getPendingTasks(userId)

    fun getCompletedTasks(userId: Long): Flow<List<Task>> = taskDao.getCompletedTasks(userId)

    suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)

    suspend fun insertTask(task: Task): Long {
        val taskId = taskDao.insertTask(task)

        // Try to sync with API
        try {
            val request = TaskRequest(
                todo = task.title,
                completed = task.isCompleted,
                userId = task.userId
            )
            val response = apiService.addTodo(request)
            if (response.isSuccessful) {
                response.body()?.let { apiTodo ->
                    taskDao.updateTask(
                        task.copy(
                            id = taskId,
                            remoteId = apiTodo.id,
                            isSynced = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return taskId
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis(), isSynced = false))

        // Try to sync with API
        task.remoteId?.let { remoteId ->
            try {
                val request = TaskRequest(
                    todo = task.title,
                    completed = task.isCompleted,
                    userId = task.userId
                )
                val response = apiService.updateTodo(remoteId, request)
                if (response.isSuccessful) {
                    taskDao.markAsSynced(task.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun updateTaskStatus(taskId: Long, isCompleted: Boolean) {
        taskDao.updateTaskStatus(taskId, isCompleted)

        val task = taskDao.getTaskById(taskId)
        task?.remoteId?.let { remoteId ->
            try {
                val request = TaskRequest(
                    todo = task.title,
                    completed = isCompleted,
                    userId = task.userId
                )
                apiService.updateTodo(remoteId, request)
                taskDao.markAsSynced(taskId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)

        task.remoteId?.let { remoteId ->
            try {
                apiService.deleteTodo(remoteId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun syncFromRemote(userId: Long): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTodos(limit = 30)
            if (response.isSuccessful) {
                val todos = response.body()?.todos ?: emptyList()
                val tasks = todos.map { apiTodo ->
                    Task(
                        title = apiTodo.todo,
                        isCompleted = apiTodo.completed,
                        userId = userId,
                        remoteId = apiTodo.id,
                        isSynced = true
                    )
                }
                taskDao.insertTasks(tasks)
                Result.success(tasks.size)
            } else {
                Result.failure(Exception("Failed to fetch todos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncUnsyncedTasks() = withContext(Dispatchers.IO) {
        val unsyncedTasks = taskDao.getUnsyncedTasks()
        for (task in unsyncedTasks) {
            try {
                val request = TaskRequest(
                    todo = task.title,
                    completed = task.isCompleted,
                    userId = task.userId
                )

                val response = if (task.remoteId == null) {
                    apiService.addTodo(request)
                } else {
                    apiService.updateTodo(task.remoteId, request)
                }

                if (response.isSuccessful) {
                    response.body()?.let { apiTodo ->
                        taskDao.updateTask(
                            task.copy(
                                remoteId = apiTodo.id,
                                isSynced = true
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getTasksWithUpcomingDeadlines(): List<Task> {
        return taskDao.getTasksWithUpcomingDeadlines(System.currentTimeMillis())
    }
}

