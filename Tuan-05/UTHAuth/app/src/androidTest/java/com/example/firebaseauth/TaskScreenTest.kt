package com.example.firebaseauth

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.firebaseauth.data.*
import com.example.firebaseauth.ui.*
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun checkboxReflectsUserActionAndOpensEditor() {
        var edited = false
        compose.setContent {
            var task by remember { mutableStateOf(TaskItem(id = "test", title = "Nộp bài")) }
            FirebaseAuthTheme {
                TaskRow(task, { task = task.copy(completed = !task.completed) }, { edited = true })
            }
        }
        compose.onNodeWithContentDescription("Hoàn thành: Nộp bài").assertIsOff().performClick().assertIsOn()
        compose.onNodeWithContentDescription("Sửa Nộp bài").performClick()
        compose.runOnIdle { org.junit.Assert.assertTrue(edited) }
    }

    @Test fun progressDisplaysHalfComplete() {
        compose.setContent { FirebaseAuthTheme { ProgressHero(Progress(2, 4), "Cố lên!") } }
        compose.onNodeWithText("2/4 việc").assertIsDisplayed()
        compose.onNodeWithText("50%").assertIsDisplayed()
    }

    @Test fun invalidEditorInputDisablesSave() {
        compose.setContent {
            FirebaseAuthTheme { TaskEditor(TaskItem(), {}, {}, {}, {}) }
        }
        compose.onNodeWithText("Lưu công việc").assertIsNotEnabled()
        compose.onNodeWithText("Tên công việc").performTextInput("Nộp bài")
        compose.onNodeWithText("Lưu công việc").assertIsEnabled()
        compose.onNodeWithText("Giờ (HH:mm)").performTextReplacement("25:00")
        compose.onNodeWithText("Lưu công việc").assertIsNotEnabled()
    }
}
