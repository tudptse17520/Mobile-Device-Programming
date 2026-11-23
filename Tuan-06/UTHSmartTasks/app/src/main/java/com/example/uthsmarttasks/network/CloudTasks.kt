package com.example.uthsmarttasks.network

import com.example.uthsmarttasks.model.PatchTaskRequest
import com.example.uthsmarttasks.model.Subtask
import com.example.uthsmarttasks.model.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges

class CloudTasks(uid: String) {
    private val tasks = FirebaseFirestore.getInstance().collection("users").document(uid).collection("tasks")

    fun observe(onTasks: (List<Task>, Boolean, Boolean) -> Unit, onError: (Exception) -> Unit) =
        tasks.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) onError(error)
            else if (snapshot != null) runCatching {
                snapshot.documents.map { it.toTask() }.sortedWith(compareBy<Task> { it.date }.thenBy { it.time })
            }.onSuccess {
                onTasks(it, snapshot.metadata.hasPendingWrites(), snapshot.metadata.isFromCache)
            }.onFailure { onError(IllegalStateException("Không đọc được dữ liệu công việc.", it)) }
        }

    fun create(task: Task) = tasks.document(task.id.toString()).set(mapOf(
        "title" to task.title, "note" to task.note, "category" to task.category,
        "date" to task.date, "time" to task.time, "priority" to task.priority,
        "remind" to task.remind, "done" to task.done, "subtasks" to task.subtasks.asMaps()
    ))

    fun patch(id: Int, request: PatchTaskRequest) = tasks.document(id.toString()).update(
        buildMap<String, Any> {
            request.title?.let { put("title", it) }
            request.note?.let { put("note", it) }
            request.category?.let { put("category", it) }
            request.date?.let { put("date", it) }
            request.time?.let { put("time", it) }
            request.priority?.let { put("priority", it) }
            request.remind?.let { put("remind", it) }
            request.done?.let { put("done", it) }
            request.subtasks?.let { put("subtasks", it.asMaps()) }
        }
    )

    fun delete(id: Int) = tasks.document(id.toString()).delete()

    private fun List<Subtask>.asMaps() = map { mapOf("title" to it.title, "done" to it.done) }

    private fun DocumentSnapshot.toTask() = Task(
        id = id.toInt(),
        title = getString("title").orEmpty(),
        note = getString("note").orEmpty(),
        category = getString("category") ?: "study",
        date = getString("date").orEmpty(),
        time = getString("time") ?: "09:00",
        priority = getString("priority") ?: "medium",
        remind = getBoolean("remind") == true,
        done = getBoolean("done") == true,
        subtasks = (get("subtasks") as? List<*>).orEmpty().mapNotNull {
            val item = it as? Map<*, *> ?: return@mapNotNull null
            val title = item["title"] as? String ?: return@mapNotNull null
            Subtask(title, item["done"] == true)
        }
    )
}
