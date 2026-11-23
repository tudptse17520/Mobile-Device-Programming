package com.example.productapp.model

import com.google.gson.annotations.SerializedName

/**
 * Data class đại diện cho một sản phẩm từ API.
 * Sử dụng @SerializedName để map tên field JSON với property Kotlin.
 */
data class Product(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("des")
    val description: String = "",

    @SerializedName("price")
    val price: Double = 0.0,

    @SerializedName("imgURL")
    val imageUrl: String = ""
)
