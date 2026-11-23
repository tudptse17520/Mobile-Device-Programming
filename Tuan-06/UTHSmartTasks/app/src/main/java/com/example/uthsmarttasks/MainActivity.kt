package com.example.uthsmarttasks

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uthsmarttasks.auth.ui.AuthViewModel
import com.example.uthsmarttasks.notifications.TaskReminder
import com.example.uthsmarttasks.ui.TodoApp
import com.example.uthsmarttasks.ui.theme.AppTypography
import com.example.uthsmarttasks.ui.theme.todoColors
import com.example.uthsmarttasks.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        TaskReminder.createChannel(this)
        taskViewModel.enableCloudSync()
        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val settings = taskViewModel.uiState.collectAsStateWithLifecycle().value
            val isDark = settings.themeId == "dark" || (settings.themeId == "auto" && isSystemInDarkTheme())
            val colors = todoColors(isDark, settings.accentId)
            SideEffect {
                window.statusBarColor = if (isDark) android.graphics.Color.BLACK else android.graphics.Color.rgb(245, 245, 247)
                window.navigationBarColor = if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
            }
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, settings.fontScale)) {
                MaterialTheme(colorScheme = colors, typography = AppTypography) {
                    TodoApp(taskViewModel, authViewModel, requestNotificationPermissionOnHome = true)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        TaskReminder.restore(this)
    }
}
