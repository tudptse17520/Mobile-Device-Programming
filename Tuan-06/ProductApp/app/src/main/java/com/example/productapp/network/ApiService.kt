package com.example.productapp.network

import com.example.productapp.model.Product
import retrofit2.http.GET

/**
 * Interface định nghĩa các endpoint API sử dụng Retrofit.
 */
interface ApiService {

    /**
     * Gọi API GET để lấy thông tin sản phẩm.
     * Endpoint: /v2/product
     */
    @GET("v2/product")
    suspend fun getProduct(): Product
}
