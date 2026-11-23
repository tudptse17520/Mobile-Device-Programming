package com.example.firebaseauth.data

data class Account(
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val provider: String = "Google"
)
