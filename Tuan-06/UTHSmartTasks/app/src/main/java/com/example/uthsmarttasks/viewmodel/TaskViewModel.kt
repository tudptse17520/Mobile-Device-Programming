package com.example.uthsmarttasks.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.uthsmarttasks.BuildConfig
import com.example.uthsmarttasks.model.PatchTaskRequest
import com.example.uthsmarttasks.model.SampleTasks
import com.example.uthsmarttasks.model.Subtask
import com.example.uthsmarttasks.model.Task
import com.example.uthsmarttasks.model.CreateTaskRequest
import com.example.uthsmarttasks.model.changesFrom
import com.example.uthsmarttasks.model.applyTo
import com.example.uthsmarttasks.network.TaskRepository
import com.example.uthsmarttasks.network.TaskApiException
import com.example.uthsmarttasks.network.ApiClient
import com.example.uthsmarttasks.network.CloudTasks
import com.example.uthsmarttasks.network.SyncDiagnostics
import com.example.uthsmarttasks.notifications.TaskReminder
import com.google.android.gms.tasks.Task as FirebaseOperation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class TaskUiState(
    val tasks: List<Task> = if (BuildConfig.MOCK_API_TOKEN.isBlank()) SampleTasks.items else emptyList(),
    val loading: Boolean = false,
    val message: String? = null,
    val messageIsError: Boolean = false,
    val taskListLoadFailed: Boolean = false,
    val cloudUid: String? = null,
    val syncStatus: String? = null,
    val themeId: String = "light",
    val accentId: String = "blue",
    val fontScale: Float = 1f,
    val remindersEnabled: Boolean = true,
    val notificationSound: Boolean = true,
    val dailySummary: Boolean = false,
    val demoMode: Boolean = BuildConfig.MOCK_API_TOKEN.isBlank()
) {
    val darkTheme: Boolean get() = themeId == "dark"
}

class TaskViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: TaskRepository = ApiClient.taskRepository,
    private val offlineDemo: Boolean = BuildConfig.MOCK_API_TOKEN.isBlank()
) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("uth_todo_settings", Application.MODE_PRIVATE)
    private val initialTheme = preferences.getString("theme", "light") ?: "light"
    private val _uiState = MutableStateFlow(
        TaskUiState(
            tasks = if (offlineDemo) SampleTasks.items else emptyList(),
            demoMode = offlineDemo,
            themeId = initialTheme,
            accentId = preferences.getString("accent", "blue") ?: "blue",
            fontScale = preferences.getFloat("font_scale", 1f),
            remindersEnabled = preferences.getBoolean("reminders", true),
            notificationSound = preferences.getBoolean("notification_sound", true),
            dailySummary = preferences.getBoolean("daily_summary", false)
        )
    )
    val uiState = _uiState.asStateFlow()
    private var firebaseAuth: FirebaseAuth? = null
    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var cloud: CloudTasks? = null
    private var cloudListener: ListenerRegistration? = null
    private var remoteJob: Job? = null
    private var sourceVersion = 0
    private var sourceInitialized = false
    private var guestSessionStarted = false

    init {
        TaskReminder.scheduleDailySummary(getApplication(), _uiState.value.dailySummary)
    }

    fun enableCloudSync() {
        if (authListener != null) return
        val auth = FirebaseAuth.getInstance()
        firebaseAuth = auth
        switchSource(auth.currentUser?.uid)
        authListener = FirebaseAuth.AuthStateListener {
            val uid = it.currentUser?.uid
            if (uid != _uiState.value.cloudUid) switchSource(uid)
        }.also(auth::addAuthStateListener)
    }

    private fun switchSource(uid: String?) {
        val firstStart = !sourceInitialized
        sourceInitialized = true
        guestSessionStarted = false
        sourceVersion++
        remoteJob?.cancel()
        cloudListener?.remove()
        cloudListener = null
        cloud = uid?.let(::CloudTasks)
        TaskReminder.useAccount(getApplication(), uid)
        _uiState.update {
            it.copy(
                tasks = if (uid == null && offlineDemo) SampleTasks.items else emptyList(),
                cloudUid = uid, demoMode = uid == null && offlineDemo,
                loading = false, taskListLoadFailed = false,
                message = null, messageIsError = false, syncStatus = null
            )
        }
        if (!firstStart) TaskReminder.scheduleDailySummary(getApplication(), _uiState.value.dailySummary)
        if (uid != null) loadTasks()
    }

    fun startGuestSession() {
        if (guestSessionStarted || cloud != null || firebaseAuth?.currentUser != null) return
        guestSessionStarted = true
        if (offlineDemo) scheduleReminders()
        loadTasks()
    }

    private fun listenToCloud(source: CloudTasks) {
        cloudListener?.remove()
        _uiState.update { it.copy(loading = true, syncStatus = "Đang tải…") }
        cloudListener = source.observe(onTasks = { tasks, pending, cached ->
            if (cloud === source) {
                _uiState.update {
                    it.copy(tasks = tasks, loading = false, taskListLoadFailed = false, syncStatus = when {
                        pending -> "Đang đồng bộ…"
                        cached -> "Dữ liệu trên máy"
                        else -> "Đã đồng bộ"
                    })
                }
                scheduleReminders()
            }
        }, onError = { error ->
            if (cloud === source) {
                SyncDiagnostics.report("read", error)
                _uiState.update {
                    val permissionDenied = (error as? FirebaseFirestoreException)?.code ==
                        FirebaseFirestoreException.Code.PERMISSION_DENIED
                    it.copy(
                        tasks = if (permissionDenied) emptyList() else it.tasks,
                        loading = false, taskListLoadFailed = true, syncStatus = "Chưa đồng bộ",
                        message = if (permissionDenied) "Firestore chưa cho phép tài khoản đọc công việc."
                        else "Không tải được công việc từ Firestore. Vui lòng thử lại.",
                        messageIsError = true
                    )
                }
                scheduleReminders()
            }
        })
    }

    private fun observeWrite(operation: FirebaseOperation<Void>, source: CloudTasks, action: String) {
        operation.addOnFailureListener {
            if (cloud === source) {
                SyncDiagnostics.report(action, it)
                _uiState.update { it.copy(syncStatus = "Chưa đồng bộ", message = "Không lưu được thay đổi lên Firestore. Vui lòng thử lại.", messageIsError = true) }
            }
        }
    }

    override fun onCleared() {
        cloudListener?.remove()
        authListener?.let { firebaseAuth?.removeAuthStateListener(it) }
        super.onCleared()
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun loadTask(id: Int, onMissing: () -> Unit = {}) {
        if (cloud != null || _uiState.value.demoMode || _uiState.value.loading) return
        runRemote(null, onMissing = onMissing) {
            repository.loadTask(id)
        }
    }

    fun loadTasks() {
        if (_uiState.value.loading) return
        cloud?.let { listenToCloud(it); return }
        if (_uiState.value.demoMode) {
            _uiState.update { it.copy(message = "Đang dùng dữ liệu mẫu trên máy") }
            return
        }
        runRemote(null, isTaskListLoad = true) { repository.loadTasks() }
    }

    fun loadTasksOnListEntry() {
        if (cloud == null && !_uiState.value.demoMode) loadTasks()
    }

    fun toggleDone(task: Task) {
        val current = currentTask(task)
        patch(current, PatchTaskRequest(done = !current.done),
            if (current.done) "Đã đánh dấu chưa xong" else "Tuyệt, đã xong một việc!")
    }

    fun createTask(request: CreateTaskRequest, onSuccess: (Int) -> Unit = {}) {
        if (_uiState.value.loading) return
        cloud?.let { source ->
            if (_uiState.value.taskListLoadFailed) {
                _uiState.update { it.copy(message = "Hãy tải lại công việc từ Firestore trước khi thêm.", messageIsError = true) }
                return
            }
            val tasks = _uiState.value.tasks
            val id = generateSequence { Random.nextInt(1, Int.MAX_VALUE) }.first { id -> tasks.none { it.id == id } }
            val task = Task(id, request.title, request.note, request.category, request.date,
                request.time, request.priority, request.remind)
            observeWrite(source.create(task), source, "create")
            _uiState.update { it.copy(tasks = it.tasks + task, syncStatus = "Đang đồng bộ…") }
            scheduleReminders()
            onSuccess(id)
            return
        }
        if (_uiState.value.demoMode) {
            val newId = (_uiState.value.tasks.maxOfOrNull { it.id } ?: 0) + 1
            val task = Task(newId, request.title, request.note, request.category, request.date,
                request.time, request.priority, request.remind)
            _uiState.update { it.copy(tasks = it.tasks + task, message = "Đã thêm công việc") }
            scheduleReminders()
            onSuccess(newId)
            return
        }
        runRemote("Đã thêm công việc") { onSuccess(repository.create(request).id) }
    }

    fun updateTask(task: Task, request: CreateTaskRequest, onSuccess: () -> Unit = {}) {
        val current = currentTask(task)
        val changes = request.changesFrom(current)
        if (changes == PatchTaskRequest()) { onSuccess(); return }
        patch(current, changes, "Đã lưu thay đổi", onSuccess)
    }

    private fun currentTask(task: Task) = _uiState.value.tasks.firstOrNull { it.id == task.id } ?: task

    fun addSubtask(task: Task, title: String) {
        if (title.isBlank()) return
        val current = currentTask(task)
        patch(current, PatchTaskRequest(subtasks = current.subtasks + Subtask(title.trim())), "Đã thêm việc nhỏ")
    }

    fun toggleSubtask(task: Task, index: Int) {
        val current = currentTask(task)
        patch(current, PatchTaskRequest(subtasks = current.subtasks.mapIndexed { i, item ->
            if (i == index) item.copy(done = !item.done) else item
        }), "Đã cập nhật việc nhỏ")
    }

    fun editSubtask(task: Task, index: Int, title: String) {
        if (title.isBlank()) return
        val current = currentTask(task)
        patch(current, PatchTaskRequest(subtasks = current.subtasks.mapIndexed { i, item ->
            if (i == index) item.copy(title = title.trim()) else item
        }), "Đã cập nhật việc nhỏ")
    }

    fun deleteSubtask(task: Task, index: Int) {
        val current = currentTask(task)
        patch(current, PatchTaskRequest(subtasks = current.subtasks.filterIndexed { i, _ -> i != index }), "Đã xóa việc nhỏ")
    }

    private fun patch(task: Task, request: PatchTaskRequest, message: String, onSuccess: () -> Unit = {}) {
        if (_uiState.value.loading) return
        cloud?.let { source ->
            if (_uiState.value.taskListLoadFailed || _uiState.value.tasks.none { it.id == task.id }) return
            observeWrite(source.patch(task.id, request), source, "update")
            replaceTask(request.applyTo(task))
            _uiState.update { it.copy(syncStatus = "Đang đồng bộ…") }
            onSuccess()
            return
        }
        if (_uiState.value.demoMode) {
            updateLocal(request.applyTo(task), message)
            onSuccess()
        } else runRemote(message) {
            repository.patch(task.id, request)
            onSuccess()
        }
    }

    fun deleteTask(task: Task, onSuccess: () -> Unit = {}) {
        if (_uiState.value.loading) return
        cloud?.let { source ->
            if (_uiState.value.taskListLoadFailed || _uiState.value.tasks.none { it.id == task.id }) return
            observeWrite(source.delete(task.id), source, "delete")
            _uiState.update { it.copy(tasks = it.tasks.filterNot { item -> item.id == task.id }, syncStatus = "Đang đồng bộ…") }
            scheduleReminders()
            onSuccess()
            return
        }
        if (_uiState.value.demoMode) {
            _uiState.update { it.copy(tasks = it.tasks.filterNot { item -> item.id == task.id }, message = "Đã xóa công việc") }
            scheduleReminders()
            onSuccess()
        } else runRemote("Đã xóa công việc") {
            repository.delete(task.id)
            onSuccess()
        }
    }

    fun setTheme(themeId: String) {
        if (themeId !in setOf("light", "dark", "auto")) return
        preferences.edit().putString("theme", themeId).apply()
        _uiState.update { it.copy(themeId = themeId) }
    }

    fun setAccent(accentId: String) {
        if (accentId !in setOf("blue", "green", "orange", "purple")) return
        preferences.edit().putString("accent", accentId).apply()
        _uiState.update { it.copy(accentId = accentId) }
    }

    fun setFontScale(scale: Float) {
        val safeScale = scale.coerceIn(.85f, 1.2f)
        preferences.edit().putFloat("font_scale", safeScale).apply()
        _uiState.update { it.copy(fontScale = safeScale) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        preferences.edit().putBoolean("reminders", enabled).apply()
        _uiState.update { it.copy(remindersEnabled = enabled) }
        scheduleReminders()
    }

    fun setNotificationSound(enabled: Boolean) {
        preferences.edit().putBoolean("notification_sound", enabled).apply()
        _uiState.update { it.copy(notificationSound = enabled) }
    }

    fun setDailySummary(enabled: Boolean) {
        preferences.edit().putBoolean("daily_summary", enabled).apply()
        _uiState.update { it.copy(dailySummary = enabled) }
        TaskReminder.scheduleDailySummary(getApplication(), enabled)
    }

    fun toggleTheme() = setTheme(if (_uiState.value.darkTheme) "light" else "dark")

    fun scheduleReminders() {
        val state = _uiState.value
        TaskReminder.sync(getApplication(), state.tasks, state.remindersEnabled)
    }

    private fun updateLocal(task: Task, message: String) {
        replaceTask(task)
        _uiState.update { it.copy(message = message) }
    }

    private fun replaceTask(task: Task) {
        _uiState.update { state -> state.copy(tasks = state.tasks.map { if (it.id == task.id) task else it }) }
        scheduleReminders()
    }

    private fun runRemote(successMessage: String?, onMissing: () -> Unit = {}, isTaskListLoad: Boolean = false, action: suspend () -> Unit) {
        if (_uiState.value.loading) return
        _uiState.update { it.copy(loading = true, message = null, messageIsError = false) }
        val version = sourceVersion
        remoteJob = viewModelScope.launch {
            try {
                action()
                if (version != sourceVersion) return@launch
                val tasks = repository.snapshot()
                _uiState.update { it.copy(tasks = tasks, loading = false, message = successMessage, messageIsError = false, taskListLoadFailed = if (isTaskListLoad) false else it.taskListLoadFailed) }
                scheduleReminders()
            } catch (e: CancellationException) {
                if (version == sourceVersion) _uiState.update { it.copy(loading = false) }
                throw e
            } catch (e: TaskApiException) {
                if (e.status == 404) onMissing()
                _uiState.update { it.copy(loading = false, message = e.message, messageIsError = true, taskListLoadFailed = isTaskListLoad || it.taskListLoadFailed) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, message = "Không thể kết nối hoặc đọc dữ liệu API. Vui lòng thử lại.", messageIsError = true, taskListLoadFailed = isTaskListLoad || it.taskListLoadFailed) }
            }
        }
    }
}
