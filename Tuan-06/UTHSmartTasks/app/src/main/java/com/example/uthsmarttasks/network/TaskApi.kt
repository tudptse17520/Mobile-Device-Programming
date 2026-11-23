package com.example.uthsmarttasks.network

import com.example.uthsmarttasks.model.PatchTaskRequest
import com.example.uthsmarttasks.model.Task
import com.example.uthsmarttasks.model.CreateTaskRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface TaskApi {
    @GET("tasks")
    suspend fun getTasks(): Response<List<Task>>

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: Int): Response<Task>

    @POST("tasks")
    suspend fun createTask(@Body task: CreateTaskRequest): Response<Task>

    @PATCH("tasks/{id}")
    suspend fun updateTask(@Path("id") id: Int, @Body task: PatchTaskRequest): Response<Task>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<Unit>
}
