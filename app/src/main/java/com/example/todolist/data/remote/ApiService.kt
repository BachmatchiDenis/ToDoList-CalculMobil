package com.example.todolist.data.remote

import com.example.todolist.model.*
import retrofit2.Response
import retrofit2.http.*

interface TodoApiService {

    // Get all todos
    @GET("todos")
    suspend fun getAllTodos(): Response<TodosResponse>

    // Get todos with pagination
    @GET("todos")
    suspend fun getTodos(
        @Query("limit") limit: Int = 30,
        @Query("skip") skip: Int = 0
    ): Response<TodosResponse>

    // Get todos by user
    @GET("todos/user/{userId}")
    suspend fun getTodosByUser(@Path("userId") userId: Long): Response<TodosResponse>

    // Get single todo
    @GET("todos/{id}")
    suspend fun getTodoById(@Path("id") id: Long): Response<ApiTodo>

    // Add new todo
    @POST("todos/add")
    suspend fun addTodo(@Body request: TaskRequest): Response<ApiTodo>

    // Update todo
    @PUT("todos/{id}")
    suspend fun updateTodo(
        @Path("id") id: Long,
        @Body request: TaskRequest
    ): Response<ApiTodo>

    // Delete todo
    @DELETE("todos/{id}")
    suspend fun deleteTodo(@Path("id") id: Long): Response<ApiTodo>
}

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}

