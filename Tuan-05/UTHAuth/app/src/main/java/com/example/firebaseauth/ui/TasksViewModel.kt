package com.example.firebaseauth.ui

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseauth.data.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class TasksState(
    val tasks: List<TaskItem> = emptyList(),
    val loading: Boolean = false,
    val connected: Boolean = false,
    val error: String? = null,
    val databaseUrl: String = "",
    val quoteStart: String = "Khởi đầu ngày mới năng suất nhé!",
    val quoteHalf: String = "Đã hơn nửa chặng, cố lên!",
    val quoteDone: String = "Xuất sắc! Bạn đã hoàn thành mọi mục tiêu hôm nay!"
)

class TasksViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("uth_config", 0)
    private val analytics = FirebaseAnalytics.getInstance(application)
    private val mutable = MutableStateFlow(TasksState(databaseUrl = prefs.getString("database_url", "").orEmpty()))
    val state = mutable.asStateFlow()
    private var repository: FirebaseTaskRepository? = null
    private var subscription: Job? = null
    private var uid: String? = null

    fun bind(userId: String?) {
        if (uid == userId && repository != null) return
        subscription?.cancel()
        repository = null
        uid = userId
        analytics.setUserId(userId)
        mutable.update { it.copy(tasks = emptyList(), error = null, connected = false, loading = false) }
        if (userId == null || mutable.value.databaseUrl.isBlank()) return
        subscription = viewModelScope.launch {
            try {
                val repo = FirebaseTaskRepository(mutable.value.databaseUrl, userId)
                repository = repo
                mutable.update { it.copy(loading = true) }
                launch { repo.connection.catch { e -> message(e.message ?: "Lỗi kết nối") }.collect { online ->
                    mutable.update { it.copy(connected = online) }
                } }
                repo.changes.collect { tasks -> mutable.update { it.copy(tasks = tasks, loading = false, error = null) } }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutable.update { it.copy(loading = false, error = "Không đọc được công việc: ${e.message}. Kiểm tra URL và Database Rules.") } }
        }
    }

    fun configure(url: String) {
        val value = url.trim().trimEnd('/')
        if (!Regex("https://[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\\.(firebaseio\\.com|firebasedatabase\\.app)").matches(value)) {
            message("URL cần dạng https://DATABASE.firebaseio.com hoặc https://DATABASE.REGION.firebasedatabase.app")
            return
        }
        prefs.edit().putString("database_url", value).apply()
        mutable.update { it.copy(databaseUrl = value) }
        val current = uid
        repository = null
        bind(current)
    }

    fun save(task: TaskItem) {
        if (!validTask(task.title, task.time, task.date)) { message("Kiểm tra tiêu đề, giờ HH:mm và ngày yyyy-MM-dd."); return }
        val repo = repository ?: return message("Hãy cấu hình URL Realtime Database trong Cài đặt.")
        repo.save(task.copy(title = task.title.trim())) { error ->
            if (error != null) message("Lưu thất bại: ${error.message}")
            else log(if (task.id.isBlank()) "task_created" else "task_updated")
        }
    }
    fun toggle(task: TaskItem) {
        repository?.toggle(task) { e ->
            if (e != null) message("Cập nhật thất bại: ${e.message}")
            else log(if (task.completed) "task_reopened" else "task_completed")
        }
    }
    fun delete(task: TaskItem) {
        repository?.delete(task) { e ->
            if (e != null) message("Xóa thất bại: ${e.message}") else log("task_deleted")
        }
    }
    fun addSamples() {
        val tasks = mutable.value.tasks
        sampleTasks().map { it.copy(id = it.id + "-" + today()) }.filter { sample -> tasks.none { it.id == sample.id } }.forEach(::save)
    }
    fun fetchConfig() {
        val remote = FirebaseRemoteConfig.getInstance()
        remote.fetchAndActivate().addOnCompleteListener { result ->
            if (!result.isSuccessful) message("Chưa tải được Remote Config; đang dùng giá trị mặc định.")
            else {
                mutable.update { it.copy(
                    quoteStart = remote.getString("motivational_quote_start"),
                    quoteHalf = remote.getString("motivational_quote_halfway"),
                    quoteDone = remote.getString("motivational_quote_done")
                ) }
                message("Đã cập nhật Remote Config.")
            }
        }
    }
    fun message(text: String) { mutable.update { it.copy(error = text) } }
    fun dismissMessage() { mutable.update { it.copy(error = null) } }
    fun log(event: String, category: String? = null) {
        analytics.logEvent(event, Bundle().apply { category?.let { putString("category", it) } })
    }
}
