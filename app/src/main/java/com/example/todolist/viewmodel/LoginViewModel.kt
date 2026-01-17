package com.example.todolist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.repository.UserRepository
import com.example.todolist.model.User
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    private val _registerResult = MutableLiveData<Result<User>>()
    val registerResult: LiveData<Result<User>> = _registerResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        checkLoggedInUser()
    }

    private fun checkLoggedInUser() {
        viewModelScope.launch {
            _currentUser.value = userRepository.getLoggedInUser()
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginResult.value = Result.failure(Exception("Please fill in all fields"))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _loginResult.value = userRepository.login(username, password)
            _isLoading.value = false
        }
    }

    fun register(username: String, password: String, email: String) {
        if (username.isBlank() || password.isBlank()) {
            _registerResult.value = Result.failure(Exception("Please fill in all fields"))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _registerResult.value = userRepository.register(username, password, email)
            _isLoading.value = false
        }
    }
}

class LoginViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

