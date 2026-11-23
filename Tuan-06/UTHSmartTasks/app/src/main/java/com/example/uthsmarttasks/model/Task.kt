package com.example.uthsmarttasks.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class Task(
    val id: Int,
    val title: String,
    val note: String = "",
    val category: String = "study",
    val date: String = "",
    val time: String = "09:00",
    val priority: String = "medium",
    val remind: Boolean = false,
    val done: Boolean = false,
    val subtasks: List<Subtask> = emptyList()
)

data class Subtask(
    val title: String,
    val done: Boolean = false
)

data class CreateTaskRequest(
    val title: String,
    val note: String = "",
    val category: String = "study",
    val date: String = "",
    val time: String = "19:00",
    val priority: String = "medium",
    val remind: Boolean = true
)

data class PatchTaskRequest(
    val title: String? = null,
    val note: String? = null,
    val category: String? = null,
    val date: String? = null,
    val time: String? = null,
    val priority: String? = null,
    val remind: Boolean? = null,
    val done: Boolean? = null,
    val subtasks: List<Subtask>? = null
)

object SampleTasks {
    val items: List<Task> by lazy {
        fun dateAfter(days: Int): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, days) }.time
        )
        val today = dateAfter(0)
        listOf(
            Task(4, "Tập thể dục 30 phút", category = "life", date = today, time = "06:30", priority = "medium", remind = true, done = true),
            Task(1, "Nộp bài tập Lập trình di động", "Nộp file .zip lên hệ thống của trường. Nhớ kèm ảnh chụp màn hình app.", "study", today, "09:00", "high", true, true,
                listOf(Subtask("Hoàn thiện giao diện", true), Subtask("Kiểm tra chức năng", true), Subtask("Nộp file", true))),
            Task(2, "Ôn chương 3 – Cấu trúc dữ liệu", "Tập trung phần cây nhị phân và bảng băm.", "study", today, "14:00", "medium", true, false,
                listOf(Subtask("Đọc lại slide bài giảng", true), Subtask("Làm 10 bài tập cuối chương", false))),
            Task(3, "Đi chợ mua đồ cho tuần", category = "life", date = today, time = "17:30", priority = "low"),
            Task(6, "Gọi điện về nhà", category = "life", date = dateAfter(1), time = "21:00", priority = "low", remind = true),
            Task(5, "Họp nhóm đồ án", "Chuẩn bị phần demo giao diện.", "study", dateAfter(5), "20:00", "high", true)
        )
    }
}

fun CreateTaskRequest.changesFrom(task: Task) = PatchTaskRequest(
    title = title.takeIf { it != task.title },
    note = note.takeIf { it != task.note },
    category = category.takeIf { it != task.category },
    date = date.takeIf { it != task.date },
    time = time.takeIf { it != task.time },
    priority = priority.takeIf { it != task.priority },
    remind = remind.takeIf { it != task.remind }
)

fun PatchTaskRequest.applyTo(task: Task) = task.copy(
    title = title ?: task.title, note = note ?: task.note,
    category = category ?: task.category, date = date ?: task.date,
    time = time ?: task.time, priority = priority ?: task.priority,
    remind = remind ?: task.remind, done = done ?: task.done,
    subtasks = subtasks ?: task.subtasks
)

fun tasksForToday(tasks: List<Task>, date: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())) =
    tasks.filter { it.date == date }.sortedBy { it.time }
