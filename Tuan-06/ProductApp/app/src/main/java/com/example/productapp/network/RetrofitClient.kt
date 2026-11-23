package com.example.productapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object cung cấp instance Retrofit.
 * Sử dụng pattern lazy initialization để tạo instance chỉ khi cần.
 */
object RetrofitClient {

    // Base URL của API (mock server)
    private const val BASE_URL = "https://mock.apidog.com/m1/890655-872447-default/"

    // Tạo Retrofit instance với Gson converter
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Tạo ApiService instance từ Retrofit
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
