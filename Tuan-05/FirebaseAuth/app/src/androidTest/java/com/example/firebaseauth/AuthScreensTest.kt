package com.example.firebaseauth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.firebaseauth.ui.LoginScreen
import com.example.firebaseauth.ui.ProfileScreen
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loading_disables_google_and_guest_actions() {
        composeRule.setContent {
            FirebaseAuthTheme {
                LoginScreen(
                    isLoading = true,
                    errorMessage = null,
                    onGoogleSignIn = {},
                    onTryWithoutAccount = {},
                    onClose = {}
                )
            }
        }
        composeRule.onNodeWithText("Tiếp tục với Google").assertIsNotEnabled()
        composeRule.onNodeWithText("Dùng thử không cần tài khoản").assertIsNotEnabled()
    }

    @Test
    fun error_is_visible_and_sign_in_can_be_retried() {
        var signInRequests = 0
        val message = "Bạn đã huỷ đăng nhập bằng Google."
        composeRule.setContent {
            FirebaseAuthTheme {
                LoginScreen(
                    isLoading = false,
                    errorMessage = message,
                    onGoogleSignIn = { signInRequests++ },
                    onTryWithoutAccount = {},
                    onClose = {}
                )
            }
        }
        composeRule.onNodeWithText(message).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Tiếp tục với Google").performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals(1, signInRequests) }
    }

    @Test
    fun short_window_keeps_sign_in_and_guest_actions_reachable() {
        var signInRequests = 0
        var guestRequests = 0
        composeRule.setContent {
            FirebaseAuthTheme {
                Box(Modifier.requiredSize(390.dp, 400.dp)) {
                    LoginScreen(
                        isLoading = false,
                        errorMessage = "Không có kết nối mạng. Vui lòng kiểm tra Internet và thử lại.",
                        onGoogleSignIn = { signInRequests++ },
                        onTryWithoutAccount = { guestRequests++ },
                        onClose = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("Tiếp tục với Google").performScrollTo().performClick()
        composeRule.onNodeWithText("Dùng thử không cần tài khoản").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(1, signInRequests)
            assertEquals(1, guestRequests)
        }
    }

    @Test
    fun profile_displays_supplied_account_and_dispatches_sign_out() {
        var signOutRequests = 0
        composeRule.setContent {
            FirebaseAuthTheme {
                ProfileScreen(
                    name = "Sinh viên kiểm thử",
                    email = "student@example.com",
                    photoUrl = null,
                    linked = true,
                    onBack = {},
                    onSignOut = { signOutRequests++ }
                )
            }
        }
        composeRule.onNodeWithText("Sinh viên kiểm thử").assertIsDisplayed()
        composeRule.onNodeWithText("student@example.com").assertIsDisplayed()
        composeRule.onNodeWithText("Đã liên kết tài khoản Google").assertIsDisplayed()
        composeRule.onNodeWithText("Đăng xuất").performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals(1, signOutRequests) }
    }
}
