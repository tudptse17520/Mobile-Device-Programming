package com.example.firebaseauth.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseauth.model.AuthUiState
import com.example.firebaseauth.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var isSigningOut = false

    init {
        checkCurrentAuth()
    }

    private fun checkCurrentAuth() {
        val user = repository.currentUser
        _uiState.value = if (user != null) {
            AuthUiState.Success(
                email = user.email.orEmpty(),
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString()
            )
        } else {
            AuthUiState.Idle
        }
    }

    // Gọi Credential Manager, cần Activity context để hiện bảng chọn tài khoản
    fun signInWithGoogle(activityContext: Context) {
        if (_uiState.value is AuthUiState.Loading || isSigningOut) return
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = try {
                repository.signInWithGoogle(activityContext)
            } catch (error: CancellationException) {
                checkCurrentAuth()
                throw error
            }
            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState.Success(
                        email = user.email.orEmpty(),
                        displayName = user.displayName,
                        photoUrl = user.photoUrl?.toString()
                    )
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(
                        message = error.message ?: "Đăng nhập Google thất bại."
                    )
                }
            )
        }
    }

    fun signOut(context: Context) {
        if (isSigningOut || _uiState.value is AuthUiState.Loading) return
        isSigningOut = true
        val applicationContext = context.applicationContext
        viewModelScope.launch {
            try {
                repository.signOut(applicationContext)
            } finally {
                // Firebase đã đăng xuất trước khi xóa trạng thái Credential Manager.
                _uiState.value = AuthUiState.Idle
                isSigningOut = false
            }
        }
    }

    fun dismissError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}
