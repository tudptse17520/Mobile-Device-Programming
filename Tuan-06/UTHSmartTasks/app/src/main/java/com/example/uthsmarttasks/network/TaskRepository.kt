package com.example.uthsmarttasks.network

import com.example.uthsmarttasks.model.*
import com.google.gson.Gson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import java.io.IOException

class TaskApiException(message: String, val status: Int) : IOException(message)

fun Response<*>.requireSuccess() {
    if (isSuccessful) return
    val message = runCatching {
        Gson().fromJson(errorBody()?.string(), ApiError::class.java)?.message?.takeIf { it.isNotBlank() }
    }.getOrNull()
    throw TaskApiException(message ?: "Yêu cầu không thành công (HTTP ${code()}). Vui lòng thử lại.", code())
}

private data class ApiError(val message: String?)

// TryApps serves fixed samples. These overlays live only for this repository's session.
class TaskRepository(private val api: TaskApi) {
    private val mutex = Mutex()
    private var tasks = emptyList<Task>()
    private val changed = linkedMapOf<Int, Task>()
    private val deleted = mutableSetOf<Int>()
    private val remoteIds = mutableMapOf<Int, Int>()
    private var nextLocalId = -1

    private fun merge(server: List<Task>): List<Task> =
        (server.filterNot { it.id in deleted || it.id in changed } + changed.values)
            .sortedWith(compareBy<Task> { it.date }.thenBy { it.time })

    suspend fun loadTasks(): List<Task> = mutex.withLock {
        val response = api.getTasks()
        response.requireSuccess()
        tasks = merge(response.body() ?: throw IOException("API không trả danh sách công việc."))
        tasks
    }

    suspend fun loadTask(id: Int): Task = mutex.withLock {
        // Still issue GET for existing server tasks, but don't overwrite successful local writes.
        if (id in remoteIds) return@withLock changed.getValue(id)
        val response = api.getTask(id)
        response.requireSuccess()
        val sample = response.body() ?: throw IOException("API không trả chi tiết công việc.")
        val task = changed[id] ?: if (sample.id == id) sample else tasks.firstOrNull { it.id == id }
            ?: throw IOException("Response API không khớp mã công việc.")
        tasks = (tasks.filterNot { it.id == id } + task)
        task
    }

    suspend fun create(request: CreateTaskRequest): Task = mutex.withLock {
        val response = api.createTask(request)
        response.requireSuccess()
        val sample = response.body() ?: throw IOException("API không trả công việc vừa tạo.")
        val id = if (tasks.any { it.id == sample.id } || sample.id in deleted) nextLocalId-- else sample.id
        remoteIds[id] = sample.id
        val task = Task(id, request.title, request.note, request.category, request.date,
            request.time, request.priority, request.remind)
        changed[id] = task
        tasks = merge(tasks)
        task
    }

    suspend fun patch(id: Int, request: PatchTaskRequest): Task = mutex.withLock {
        val current = tasks.firstOrNull { it.id == id } ?: throw IOException("Không tìm thấy công việc.")
        val response = api.updateTask(remoteIds[id] ?: id, request)
        response.requireSuccess()
        // The request path identifies the task; the fixed response may contain a different ID.
        val task = request.applyTo(current)
        changed[id] = task
        tasks = merge(tasks)
        task
    }

    suspend fun delete(id: Int) = mutex.withLock {
        val response = api.deleteTask(remoteIds[id] ?: id)
        response.requireSuccess()
        deleted += id
        changed.remove(id)
        remoteIds.remove(id)
        tasks = merge(tasks)
    }

    fun snapshot(): List<Task> = tasks.toList()
}
