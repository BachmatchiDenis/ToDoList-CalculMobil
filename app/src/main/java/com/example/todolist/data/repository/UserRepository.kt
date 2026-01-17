package com.example.todolist.data.repository

import com.example.todolist.data.local.UserDao
import com.example.todolist.data.remote.AuthApiService
import com.example.todolist.model.LoginRequest
import com.example.todolist.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val userDao: UserDao,
    private val authApiService: AuthApiService
) {

    suspend fun login(username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            // First try API login
            val apiResult = tryApiLogin(username, password)
            if (apiResult.isSuccess) {
                return@withContext apiResult
            }

            // Fall back to local login
            val localUser = userDao.login(username, password)
            if (localUser != null) {
                userDao.logoutAllUsers()
                userDao.setUserLoggedIn(localUser.id)
                return@withContext Result.success(localUser.copy(isLoggedIn = true))
            }

            Result.failure(Exception("Invalid username or password"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun tryApiLogin(username: String, password: String): Result<User> {
        return try {
            val response = authApiService.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    val user = User(
                        username = loginResponse.username,
                        password = password,
                        email = loginResponse.email,
                        isLoggedIn = true
                    )

                    // Check if user exists locally
                    val existingUser = userDao.getUserByUsername(username)
                    val userId = if (existingUser != null) {
                        userDao.updateUser(existingUser.copy(isLoggedIn = true))
                        existingUser.id
                    } else {
                        userDao.logoutAllUsers()
                        userDao.insertUser(user)
                    }

                    val savedUser = userDao.getUserById(userId)
                    return Result.success(savedUser ?: user.copy(id = userId))
                }
                Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("API login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, password: String, email: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val existingUser = userDao.getUserByUsername(username)
            if (existingUser != null) {
                return@withContext Result.failure(Exception("Username already exists"))
            }

            val user = User(
                username = username,
                password = password,
                email = email,
                isLoggedIn = true
            )

            userDao.logoutAllUsers()
            val userId = userDao.insertUser(user)

            Result.success(user.copy(id = userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLoggedInUser(): User? = withContext(Dispatchers.IO) {
        userDao.getLoggedInUser()
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        userDao.logoutAllUsers()
    }
}

