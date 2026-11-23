package com.example.firebaseauth.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseAuthErrorMessageTest {

    @Test
    fun network_error_has_friendly_message() {
        assertEquals(
            "Không có kết nối mạng. Vui lòng kiểm tra Internet và thử lại.",
            firebaseAuthErrorMessage("ERROR_NETWORK_REQUEST_FAILED")
        )
    }

    @Test
    fun invalid_credential_has_friendly_message() {
        assertTrue(
            firebaseAuthErrorMessage("ERROR_INVALID_CREDENTIAL").contains("không hợp lệ")
        )
    }

    @Test
    fun unknown_error_has_fallback_message() {
        assertEquals(
            "Xác thực với Firebase thất bại. Vui lòng thử lại.",
            firebaseAuthErrorMessage("SOMETHING_ELSE")
        )
    }

    @Test
    fun null_error_has_fallback_message() {
        assertEquals(
            "Xác thực với Firebase thất bại. Vui lòng thử lại.",
            firebaseAuthErrorMessage(null)
        )
    }
}
