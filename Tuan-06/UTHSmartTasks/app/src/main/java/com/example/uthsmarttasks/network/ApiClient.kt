package com.example.uthsmarttasks.network

import com.example.uthsmarttasks.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", "Bearer ${BuildConfig.MOCK_API_TOKEN}")
                .build()
            chain.proceed(request)
        }
        .build()

    val taskRepository: TaskRepository by lazy { TaskRepository(taskApi) }

    fun create(baseUrl: String, token: String = ""): TaskApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
            if (token.isNotBlank()) request.header("Authorization", "Bearer $token")
            chain.proceed(request.build())
        }.build())
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(TaskApi::class.java)

    val taskApi: TaskApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://share.tryapps.store/mock/teach/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TaskApi::class.java)
    }
}
