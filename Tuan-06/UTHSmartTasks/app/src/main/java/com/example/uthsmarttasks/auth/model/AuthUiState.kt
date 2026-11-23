package com.example.uthsmarttasks.auth.model

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(
        val email: String,
        val displayName: String? = null,
        val photoUrl: String? = null
    ) : AuthUiState
    data class Error(
        val message: String
    ) : AuthUiState
}
