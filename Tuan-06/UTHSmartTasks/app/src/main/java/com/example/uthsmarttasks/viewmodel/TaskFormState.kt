package com.example.uthsmarttasks.viewmodel

import android.os.Bundle
import com.example.uthsmarttasks.model.CreateTaskRequest
import com.example.uthsmarttasks.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class TaskFormState(
    val taskId: Int? = null,
    val draft: CreateTaskRequest = CreateTaskRequest(title = "", date = taskDateFromToday(0))
) {
    val canSave: Boolean get() = draft.title.isNotBlank()

    fun toSavedState(): Bundle = Bundle().apply {
        if (taskId != null) putInt("taskId", taskId)
        putString("title", draft.title)
        putString("note", draft.note)
        putString("category", draft.category)
        putString("date", draft.date)
        putString("time", draft.time)
        putString("priority", draft.priority)
        putBoolean("remind", draft.remind)
    }

    companion object {
        fun forTask(task: Task?): TaskFormState {
            if (task == null) return TaskFormState()
            return TaskFormState(
                task.id,
                CreateTaskRequest(task.title, task.note, task.category, task.date, task.time, task.priority, task.remind)
            )
        }

        fun restore(bundle: Bundle): TaskFormState {
            val defaults = TaskFormState().draft
            return TaskFormState(
                taskId = bundle.getInt("taskId").takeIf { bundle.containsKey("taskId") },
                draft = CreateTaskRequest(
                    title = bundle.getString("title", defaults.title),
                    note = bundle.getString("note", defaults.note),
                    category = bundle.getString("category", defaults.category),
                    date = bundle.getString("date", defaults.date),
                    time = bundle.getString("time", defaults.time),
                    priority = bundle.getString("priority", defaults.priority),
                    remind = bundle.getBoolean("remind", defaults.remind)
                )
            )
        }
    }
}

fun taskDateFromToday(days: Int): String {
    val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, days) }
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date.time)
}
