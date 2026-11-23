package com.example.uthsmarttasks

import android.app.Application
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.WindowCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.example.uthsmarttasks.auth.ui.AuthViewModel
import com.example.uthsmarttasks.auth.repository.AuthRepository
import com.example.uthsmarttasks.model.Subtask
import com.example.uthsmarttasks.model.Task
import com.example.uthsmarttasks.network.ApiClient
import com.example.uthsmarttasks.network.TaskRepository
import com.example.uthsmarttasks.ui.TodoApp
import com.example.uthsmarttasks.ui.theme.AppTypography
import com.example.uthsmarttasks.ui.theme.todoColors
import com.example.uthsmarttasks.viewmodel.TaskViewModel
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.concurrent.TimeUnit

class TaskApiFlowTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private lateinit var vm: TaskViewModel
    private val gson = Gson()
    private fun waitLoaded() {
        ui.waitUntil(15_000) { !vm.uiState.value.loading }
        ui.waitForIdle()
    }
    private fun screenshot(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        ui.waitForIdle()
        val bitmap = ui.onRoot().captureToImage().asAndroidBitmap()
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun openingListLoadsTasksAndDetailBackShowsOrigin() {
        val context = ui.activity.application
        val settings = context.getSharedPreferences("uth_todo_settings", Application.MODE_PRIVATE)
        val previousTheme = settings.getString("theme", null)
        val testApp = FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, "designAudit")
        val auth = AuthViewModel(AuthRepository(FirebaseAuth.getInstance(testApp)))
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val detail = Task(
            id = 9101, title = "Nộp bài tập Lập trình di động",
            note = "Nộp file .zip lên hệ thống của trường. Nhớ kèm ảnh chụp màn hình app chạy trên máy ảo.",
            category = "study", date = today, time = "09:00", priority = "high",
            remind = true,
            subtasks = listOf(
                Subtask("Hoàn thiện màn hình đăng nhập", true),
                Subtask("Chụp ảnh màn hình app", true),
                Subtask("Nén file và nộp bài", false)
            )
        )
        val tasks = listOf(
            detail,
            Task(id = 9102, title = "Ôn chương 3 – Cấu trúc dữ liệu", date = today, time = "14:00"),
            Task(id = 9103, title = "Đi chợ mua đồ cho tuần", category = "life", date = today, time = "17:30"),
            Task(id = 9104, title = "Tập thể dục 30 phút", category = "life", date = today, time = "06:30", done = true)
        )
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setBody(gson.toJson(tasks)))
            val repository = TaskRepository(ApiClient.create(server.url("/").toString()))
            ui.runOnUiThread {
                WindowCompat.setDecorFitsSystemWindows(ui.activity.window, false)
                vm = TaskViewModel(context, repository, false)
                vm.setTheme("light")
            }
            ui.setContent {
                val state = vm.uiState.collectAsState().value
                MaterialTheme(colorScheme = todoColors(state.themeId == "dark"), typography = AppTypography) {
                    TodoApp(vm, auth)
                }
            }
            screenshot("ui-start")
            ui.waitUntil(10_000) { ui.onAllNodesWithText("Bỏ qua").fetchSemanticsNodes().isNotEmpty() }
            screenshot("ui-onboarding1")
            ui.onNodeWithText("Tiếp tục").performScrollTo().performClick()
            screenshot("ui-onboarding2")
            ui.onNodeWithText("Tiếp tục").performScrollTo().performClick()
            screenshot("ui-onboarding3")
            ui.onNodeWithText("Bắt đầu ngay").performScrollTo().performClick()
            screenshot("ui-login")
            ui.onNodeWithText("Dùng thử không cần tài khoản").performScrollTo().performClick()
            waitLoaded()
            assertEquals("/tasks", server.takeRequest(5, TimeUnit.SECONDS)?.path)
            screenshot("ui-home")

            server.enqueue(MockResponse().setBody(gson.toJson(detail)))
            ui.onNodeWithText(detail.title).performScrollTo().performClick()
            waitLoaded()
            assertEquals("/tasks/${detail.id}", server.takeRequest(5, TimeUnit.SECONDS)?.path)
            ui.onNodeWithText("Trang chủ").assertExists()
            screenshot("ui-detail-from-home")
            ui.onNodeWithText("Xóa công việc").performScrollTo()
            ui.onNodeWithText("Trang chủ").performScrollTo().performClick()

            server.enqueue(MockResponse().setBody(gson.toJson(tasks)))
            ui.onNodeWithText("Danh sách").performClick()
            waitLoaded()
            val request = server.takeRequest(5, TimeUnit.SECONDS)
            assertEquals("GET", request?.method)
            assertEquals("/tasks", request?.path)
            screenshot("ui-list")

            server.enqueue(MockResponse().setBody(gson.toJson(detail)))
            ui.onNodeWithText(detail.title).performClick()
            waitLoaded()
            assertEquals("/tasks/${detail.id}", server.takeRequest(5, TimeUnit.SECONDS)?.path)
            ui.onNodeWithText("Danh sách").assertExists()
            screenshot("ui-detail")
            ui.onNodeWithText("Xóa công việc").performScrollTo()
            screenshot("ui-detail-bottom")
            ui.onNodeWithText("Danh sách").performScrollTo().performClick()

            ui.onNodeWithContentDescription("Thêm mới").performClick()
            ui.onNodeWithText("Việc mới").assertExists()
            screenshot("ui-form")
            ui.onNodeWithText("Hủy").performClick()

            ui.onNodeWithText("Thông báo").performClick()
            ui.onNodeWithText("Đánh dấu đã đọc").assertExists()
            screenshot("ui-notifications")

            ui.onNodeWithText("Cài đặt").performClick()
            screenshot("ui-settings")
            ui.onNodeWithContentDescription("Hồ sơ của bạn").performClick()
            ui.onNodeWithText("Liên kết Google").assertExists()
            screenshot("ui-profile")
            ui.onNodeWithText("Cài đặt").performClick()
            ui.onNodeWithText("Tối").performScrollTo().performClick()
            ui.onNodeWithText("Trang chủ").performClick()
            screenshot("ui-home-dark")
        } finally {
            settings.edit().putString("theme", previousTheme).commit()
            tasks.forEach { com.example.uthsmarttasks.notifications.TaskReminder.cancel(context, it.id) }
            testApp.delete()
            server.shutdown()
        }
    }

    @Test fun markReminderAsReadAndKeepItAfterNavigation() {
        val server = MockWebServer()
        server.start()
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        val prefs = context.getSharedPreferences("uth_notifications", android.content.Context.MODE_PRIVATE)
        val oldRead = prefs.getStringSet("read", emptySet())?.toSet() ?: emptySet()
        val testKey = "98765:2099-12-31:19:00"
        prefs.edit().putStringSet("read", oldRead - testKey).commit()
        try {
            val task = Task(98765, "Nhắc việc kiểm thử", date = "2099-12-31", time = "19:00", remind = true)
            server.enqueue(MockResponse().setBody(gson.toJson(listOf(task))))
            val repository = TaskRepository(ApiClient.create(server.url("/").toString()))
            ui.runOnUiThread {
                vm = TaskViewModel(context, repository, false)
                vm.startGuestSession()
            }
            ui.setContent { MaterialTheme { TodoApp(vm, androidx.lifecycle.viewmodel.compose.viewModel()) } }
            waitLoaded()
            ui.waitUntil(10_000) { ui.onAllNodesWithText("Bỏ qua").fetchSemanticsNodes().isNotEmpty() || ui.onAllNodesWithText("Trang chủ").fetchSemanticsNodes().isNotEmpty() }
            if (ui.onAllNodesWithText("Bỏ qua").fetchSemanticsNodes().isNotEmpty()) ui.onNodeWithText("Bỏ qua").performClick()
            if (ui.onAllNodesWithText("Dùng thử không cần tài khoản").fetchSemanticsNodes().isNotEmpty()) ui.onNodeWithText("Dùng thử không cần tài khoản").performScrollTo().performClick()
            ui.onNodeWithText("Thông báo").performClick()
            ui.onNodeWithText("Nhắc việc kiểm thử", substring = true).assertExists().performClick()
            assertTrue(prefs.getStringSet("read", emptySet())!!.contains(testKey))
            ui.onNodeWithText("Trang chủ").performClick()
            ui.onNodeWithText("Thông báo").performClick()
            ui.onNodeWithText("Nhắc việc kiểm thử", substring = true).assertExists()
            ui.onNodeWithText("Đánh dấu đã đọc").performClick()
            assertTrue(prefs.getStringSet("read", emptySet())!!.contains(testKey))
        } finally {
            prefs.edit().putStringSet("read", oldRead).commit()
            com.example.uthsmarttasks.notifications.TaskReminder.cancel(context, 98765)
            server.shutdown()
        }
    }

    @Test fun notificationDemoShowsEmptyViewAfterTenSeconds() {
        val context = ui.activity.application
        val testApp = FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, "notificationsDemo")
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setBody("[]"))
            val repository = TaskRepository(ApiClient.create(server.url("/").toString()))
            val auth = AuthViewModel(AuthRepository(FirebaseAuth.getInstance(testApp)))
            ui.runOnUiThread { vm = TaskViewModel(context, repository, false) }
            ui.setContent {
                MaterialTheme(colorScheme = todoColors(false), typography = AppTypography) { TodoApp(vm, auth) }
            }
            ui.waitUntil(10_000) { ui.onAllNodesWithText("Bỏ qua").fetchSemanticsNodes().isNotEmpty() }
            ui.onNodeWithText("Bỏ qua").performClick()
            ui.onNodeWithText("Dùng thử không cần tài khoản").performScrollTo().performClick()
            waitLoaded()
            ui.onNodeWithText("Cài đặt").performClick()
            val demo = ui.onNodeWithText("Demo thông báo MVVM").performScrollTo()
            val openedAt = SystemClock.elapsedRealtime()
            demo.performClick()
            ui.onNodeWithText("Sắp đến hạn").assertIsDisplayed()
            ui.onNodeWithText("Đã quá hạn").assertIsDisplayed()
            ui.onNodeWithText("Làm tốt lắm!").assertIsDisplayed()
            ui.onNodeWithText("Nhắc việc", substring = false).assertIsDisplayed()
            screenshot("mvvm-notifications-list")
            ui.onNodeWithText("Đánh dấu đã đọc").performClick()
            ui.onAllNodesWithContentDescription("Chưa đọc", useUnmergedTree = true).assertCountEquals(0)

            val emptyTitle = "Chưa có thông báo nào"
            ui.waitUntil(12_000) { ui.onAllNodesWithText(emptyTitle).fetchSemanticsNodes().isNotEmpty() }
            assertTrue(SystemClock.elapsedRealtime() - openedAt >= 9_900)
            ui.onNodeWithText(emptyTitle).assertIsDisplayed()
            ui.onNodeWithText("Thêm việc cần làm").assertIsDisplayed()
            ui.onNodeWithText("Cài đặt nhắc việc").assertIsDisplayed()
            screenshot("mvvm-notifications-empty")
            ui.onNodeWithText("Chạy lại demo").performClick()
            ui.onNodeWithText("Sắp đến hạn").assertIsDisplayed()
            ui.onAllNodesWithText("Thông báo").filter(hasClickAction()).onFirst().performClick()
            ui.onNodeWithText(emptyTitle).assertIsDisplayed()
            ui.onNodeWithText("Chạy lại demo").assertDoesNotExist()
            assertEquals(1, server.requestCount)
        } finally {
            testApp.delete()
            server.shutdown()
        }
    }

    @Test fun createEditCompleteSubtasksDeleteAndErrors() {
        val server = MockWebServer()
        server.start()
        try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val oldTask = Task(2, "Old server task", date = "2026-10-20", done = true)
            val sample = Task(7, "Fixed sample", date = today)
            server.enqueue(MockResponse().setBody(gson.toJson(listOf(oldTask))).setBodyDelay(1, TimeUnit.SECONDS))
            val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
            val repository = TaskRepository(ApiClient.create(server.url("/mock/teach/").toString()))
            ui.runOnUiThread {
                vm = TaskViewModel(context, repository, false)
                vm.startGuestSession()
            }
            ui.setContent { MaterialTheme { TodoApp(vm, androidx.lifecycle.viewmodel.compose.viewModel()) } }
            waitLoaded()
            server.takeRequest()
            // Wait for splash navigation before choosing the existing session or guest flow.
            ui.waitUntil(10_000) { ui.onAllNodesWithText("Bỏ qua").fetchSemanticsNodes().isNotEmpty() || ui.onAllNodesWithText("0/0 việc").fetchSemanticsNodes().isNotEmpty() }
            if (ui.onAllNodesWithText("Bỏ qua").fetchSemanticsNodes().isNotEmpty()) {
                ui.onNodeWithText("Bỏ qua").performClick()
            }
            if (ui.onAllNodesWithText("Dùng thử không cần tài khoản").fetchSemanticsNodes().isNotEmpty()) {
                ui.onNodeWithText("Dùng thử không cần tài khoản").performScrollTo().performClick()
            }
            ui.waitUntil(10_000) { ui.onAllNodesWithText("0/0 việc").fetchSemanticsNodes().isNotEmpty() }
            ui.onNodeWithText("0/0 việc").assertExists()
            ui.onNodeWithContentDescription("Thêm mới").performClick()
            ui.onNodeWithText("Lưu").assertIsNotEnabled()
            ui.onAllNodes(hasSetTextAction())[0].performTextInput("API test create")
            server.enqueue(MockResponse().setResponseCode(400).setBody("""{"message":"Lỗi kiểm thử API."}"""))
            ui.onNodeWithText("Lưu").performClick()
            waitLoaded()
            ui.onNodeWithText("Lỗi kiểm thử API.").assertExists()
            ui.onNodeWithText("Việc mới").assertExists()
            val postError = server.takeRequest()
            assertEquals(setOf("title","note","category","date","time","priority","remind"),
                JsonParser.parseString(postError.body.readUtf8()).asJsonObject.keySet())
            screenshot("api-form-error")
            ui.onNodeWithText("Đóng").performClick()

            server.enqueue(MockResponse().setResponseCode(201).setBody(gson.toJson(sample)).setBodyDelay(1, TimeUnit.SECONDS))
            ui.onNodeWithText("Lưu").performClick()
            ui.onNodeWithText("Lưu").assertIsNotEnabled()
            assertTrue(vm.uiState.value.loading)
            waitLoaded()
            assertEquals("POST", server.takeRequest().method)
            ui.onNodeWithText("API test create").assertExists()
            ui.onNodeWithText("Đóng").performClick()
            screenshot("api-created")
            ui.onNodeWithText("Sửa").performClick()
            ui.onAllNodes(hasSetTextAction())[0].performTextReplacement("API test edited")
            server.enqueue(MockResponse().setBody(gson.toJson(sample.copy(id = 999))))
            ui.onNodeWithText("Lưu").performClick()
            waitLoaded()
            val edit = server.takeRequest()
            assertEquals("/mock/teach/tasks/7", edit.path)
            assertEquals(setOf("title"), JsonParser.parseString(edit.body.readUtf8()).asJsonObject.keySet())
            ui.onNodeWithText("API test edited").assertExists()
            ui.onNodeWithText("Đóng").performClick()

            ui.onNodeWithText("Thêm việc nhỏ").performScrollTo().performTextInput("Subtask test")
            server.enqueue(MockResponse().setBody(gson.toJson(sample.copy(id = 999))))
            ui.onNodeWithText("Thêm", useUnmergedTree = true).performScrollTo().performClick()
            waitLoaded()
            val sub = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
            assertEquals(setOf("subtasks"), sub.keySet())
            assertEquals(1, sub["subtasks"].asJsonArray.size())
            ui.onNodeWithText("Subtask test").assertExists()
            ui.onNodeWithText("Đóng").performClick()

            server.enqueue(MockResponse().setBody(gson.toJson(sample.copy(id = 999))))
            ui.onNodeWithContentDescription("Sửa việc nhỏ").performScrollTo().performClick()
            ui.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextReplacement("Subtask renamed")
            ui.onNodeWithText("Lưu", useUnmergedTree = true).performClick()
            waitLoaded()
            assertEquals("Subtask renamed", JsonParser.parseString(server.takeRequest().body.readUtf8())
                .asJsonObject["subtasks"].asJsonArray[0].asJsonObject["title"].asString)
            ui.onNodeWithText("Subtask renamed").assertExists()
            ui.onNodeWithText("Đóng").performClick()

            server.enqueue(MockResponse().setBody(gson.toJson(sample.copy(id = 999))))
            ui.onNode(isToggleable()).performScrollTo().performClick()
            waitLoaded()
            assertTrue(JsonParser.parseString(server.takeRequest().body.readUtf8())
                .asJsonObject["subtasks"].asJsonArray[0].asJsonObject["done"].asBoolean)
            ui.onNodeWithText("Đóng").performClick()

            server.enqueue(MockResponse().setBody(gson.toJson(sample.copy(id = 999))))
            ui.onNodeWithContentDescription("Xóa việc nhỏ").performScrollTo().performClick()
            waitLoaded()
            assertEquals(0, JsonParser.parseString(server.takeRequest().body.readUtf8())
                .asJsonObject["subtasks"].asJsonArray.size())
            ui.onNodeWithText("Subtask renamed").assertDoesNotExist()
            ui.onNodeWithText("Đóng").performClick()

            server.enqueue(MockResponse().setBody(gson.toJson(sample.copy(id = 999))))
            ui.onNodeWithText("Đánh dấu hoàn thành").performScrollTo().performClick()
            waitLoaded()
            assertEquals("""{"done":true}""", server.takeRequest().body.readUtf8())
            ui.onNodeWithText("Đánh dấu chưa hoàn thành").assertExists()
            ui.onNodeWithText("Đóng").performClick()
            screenshot("api-completed")

            server.enqueue(MockResponse().setBody(gson.toJson(listOf(oldTask))))
            ui.runOnUiThread { vm.loadTasks() }
            waitLoaded()
            server.takeRequest()
            assertTrue(vm.uiState.value.tasks.first { it.id == 7 }.done)
            assertEquals("API test edited", vm.uiState.value.tasks.first { it.id == 7 }.title)

            ui.onNodeWithText("Xóa công việc").performScrollTo().performClick()
            ui.onNodeWithText("Không thể hoàn tác.").assertExists()
            server.enqueue(MockResponse().setResponseCode(204))
            ui.onNodeWithText("Xóa", useUnmergedTree = true).performClick()
            waitLoaded()
            val deletion = server.takeRequest()
            assertEquals("DELETE", deletion.method)
            assertEquals(0L, deletion.bodySize)
            assertFalse(vm.uiState.value.tasks.any { it.id == 7 })
            ui.onNodeWithText("Đã xóa công việc").assertExists()
            ui.onNodeWithText("Đóng").performClick()

            server.enqueue(MockResponse().setResponseCode(500).setBody("{}"))
            ui.runOnUiThread { vm.loadTasks() }
            waitLoaded()
            server.takeRequest()
            ui.onNodeWithText("Yêu cầu không thành công (HTTP 500). Vui lòng thử lại.").assertExists()
            ui.onNodeWithText("Đóng").performClick()
            server.shutdown()
            ui.runOnUiThread { vm.loadTasks() }
            waitLoaded()
            ui.onNodeWithText("Không thể kết nối hoặc đọc dữ liệu API. Vui lòng thử lại.").assertExists()
            assertEquals(1, vm.uiState.value.tasks.size)
            screenshot("api-offline")
        } finally { runCatching { server.shutdown() } }
    }
}
