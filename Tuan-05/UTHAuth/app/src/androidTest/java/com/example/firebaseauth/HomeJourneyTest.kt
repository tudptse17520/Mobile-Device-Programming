package com.example.firebaseauth

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.firebaseauth.data.*
import com.example.firebaseauth.ui.*
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class HomeJourneyTest {
    @get:Rule val compose = androidx.compose.ui.test.junit4.createAndroidComposeRule<androidx.activity.ComponentActivity>()
    @Test fun homeProgressTracksCompletionAndCategoryFilter() {
        compose.activity.runOnUiThread { androidx.core.view.WindowCompat.getInsetsController(compose.activity.window, compose.activity.window.decorView).isAppearanceLightStatusBars = true }
        compose.setContent {
            var tasks by remember { mutableStateOf(sampleTasks()) }
            FirebaseAuthTheme {
                TasksScreen(Account(displayName = "Minh Anh"),
                    TasksState(tasks = tasks, connected = true, databaseUrl = "test"),
                    onSave = { value -> tasks = tasks + value },
                    onToggle = { value -> tasks = tasks.map { if (it.id == value.id) it.copy(completed = !it.completed) else it } },
                    onDelete = {}, onSamples = {}, onConfigure = {}, onFetch = {}, onDismiss = {}, onFilter = {}, onSignOut = {})
            }
        }
        compose.onNodeWithText("2/4 việc").assertIsDisplayed()
        compose.onNodeWithText("50%").assertIsDisplayed()
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val screenshot = instrumentation.uiAutomation.takeScreenshot()
        val dir = instrumentation.targetContext.getExternalFilesDir(null)!!
        File(dir, "uth-home.png").outputStream().use { screenshot.compress(Bitmap.CompressFormat.PNG, 100, it) }
        screenshot.recycle()
        compose.onNodeWithContentDescription("Hoàn thành: Ôn chương 3 – Cấu trúc dữ liệu")
            .performScrollTo().performClick()
        compose.onNodeWithText("75%").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Xem tất cả").performClick()
        compose.onAllNodesWithText("Học tập").onFirst().performClick()
        compose.onNodeWithText("Đi chợ mua đồ cho tuần").assertDoesNotExist()
    }
}
