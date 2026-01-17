package com.example.todolist.viewmodel

import androidx.lifecycle.*
import com.example.todolist.data.repository.TaskRepository
import com.example.todolist.data.repository.UserRepository
import com.example.todolist.model.Task
import com.example.todolist.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

enum class TaskFilter {
    ALL, PENDING, COMPLETED
}

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _filter = MutableStateFlow(TaskFilter.ALL)

    private val _userId = MutableStateFlow(1L)

    val tasks: LiveData<List<Task>> = _userId.flatMapLatest { userId ->
        _filter.flatMapLatest { filter ->
            when (filter) {
                TaskFilter.ALL -> taskRepository.getAllTasks(userId)
                TaskFilter.PENDING -> taskRepository.getPendingTasks(userId)
                TaskFilter.COMPLETED -> taskRepository.getCompletedTasks(userId)
            }
        }
    }.asLiveData()

    private val _syncResult = MutableLiveData<Result<Int>>()
    val syncResult: LiveData<Result<Int>> = _syncResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _operationResult = MutableLiveData<Result<String>>()
    val operationResult: LiveData<Result<String>> = _operationResult

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = userRepository.getLoggedInUser()
            _currentUser.value = user
            user?.let {
                _userId.value = it.id
            }
        }
    }

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun getCurrentFilter(): TaskFilter = _filter.value

    fun addTask(title: String, description: String = "", deadline: Long? = null) {
        if (title.isBlank()) {
            _operationResult.value = Result.failure(Exception("Task title cannot be empty"))
            return
        }

        viewModelScope.launch {
            try {
                val task = Task(
                    title = title,
                    description = description,
                    deadline = deadline,
                    userId = _userId.value
                )
                taskRepository.insertTask(task)
                _operationResult.value = Result.success("Task added successfully")
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.updateTask(task)
                _operationResult.value = Result.success("Task updated successfully")
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.updateTaskStatus(task.id, !task.isCompleted)
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(task)
                _operationResult.value = Result.success("Task deleted")
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun syncFromRemote() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncResult.value = taskRepository.syncFromRemote(_userId.value)
            _isLoading.value = false
        }
    }

    fun syncUnsyncedTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                taskRepository.syncUnsyncedTasks()
                _operationResult.value = Result.success("Sync completed")
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}

class TaskViewModelFactory(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

