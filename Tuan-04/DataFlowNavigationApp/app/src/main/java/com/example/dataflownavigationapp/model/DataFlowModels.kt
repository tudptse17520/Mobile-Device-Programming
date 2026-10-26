package com.example.dataflownavigationapp.model

data class PasswordResetFlowData(
    val email: String = "",
    val otp: String = "",
    val password: String = ""
) {
    fun isComplete(): Boolean {
        return email.isNotBlank() && otp.isNotBlank() && password.isNotBlank()
    }
}
