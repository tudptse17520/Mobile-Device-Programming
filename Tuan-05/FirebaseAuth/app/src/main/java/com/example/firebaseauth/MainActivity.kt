package com.example.firebaseauth

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.firebaseauth.model.AuthUiState
import com.example.firebaseauth.ui.AuthViewModel
import com.example.firebaseauth.ui.HomeScreen
import com.example.firebaseauth.ui.LoginScreen
import com.example.firebaseauth.ui.ProfileScreen
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        setContent {
            FirebaseAuthTheme {
                AppRoot(viewModel = authViewModel)
            }
        }
    }
}

private enum class Screen { Welcome, Home, Profile }

@Composable
private fun AppRoot(viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Còn phiên đăng nhập thì mở thẳng Trang chủ, tránh chớp màn Đăng nhập
    var screen by rememberSaveable {
        mutableStateOf(if (uiState is AuthUiState.Success) Screen.Home else Screen.Welcome)
    }
    var isGuestMode by rememberSaveable { mutableStateOf(false) }
    var userRequestedSignIn by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (val current = uiState) {
            is AuthUiState.Success -> {
                isGuestMode = false
                if (screen == Screen.Welcome) {
                    screen = Screen.Home
                }
                if (userRequestedSignIn) {
                    userRequestedSignIn = false
                    snackbarHostState.showSnackbar(
                        message = "Đăng nhập thành công! Chào ${current.displayName ?: "bạn"}.",
                        duration = SnackbarDuration.Short
                    )
                }
            }

            is AuthUiState.Error -> userRequestedSignIn = false

            AuthUiState.Idle -> {
                userRequestedSignIn = false
                if (!isGuestMode && screen != Screen.Welcome) {
                    screen = Screen.Welcome
                }
            }

            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val success = uiState as? AuthUiState.Success
        when (screen) {
            Screen.Welcome -> LoginScreen(
                isLoading = uiState is AuthUiState.Loading,
                errorMessage = (uiState as? AuthUiState.Error)?.message,
                onGoogleSignIn = {
                    userRequestedSignIn = true
                    viewModel.signInWithGoogle(context)
                },
                onTryWithoutAccount = {
                    isGuestMode = true
                    screen = Screen.Home
                    viewModel.dismissError()
                },
                onClose = { (context as? Activity)?.finish() }
            )

            Screen.Home -> HomeScreen(
                displayName = success?.displayName,
                photoUrl = success?.photoUrl,
                onOpenProfile = { screen = Screen.Profile }
            )

            Screen.Profile -> ProfileScreen(
                name = success?.displayName ?: if (isGuestMode) "Dùng thử" else null,
                email = success?.email,
                photoUrl = success?.photoUrl,
                linked = success != null,
                onBack = { screen = Screen.Home },
                onSignOut = {
                    if (isGuestMode) {
                        isGuestMode = false
                        screen = Screen.Welcome
                    } else {
                        viewModel.signOut(context)
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
        )
    }
}
