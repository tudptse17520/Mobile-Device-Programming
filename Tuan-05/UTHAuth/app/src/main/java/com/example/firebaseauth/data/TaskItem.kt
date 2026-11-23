package com.example.firebaseauth.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TaskItem(
    val id: String = "",
    val title: String = "",
    val category: String = "Học tập",
    val time: String = "09:00",
    val date: String = today(),
    val completed: Boolean = false,
    val note: String = ""
)

fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
fun validTask(title: String, time: String, date: String): Boolean =
    title.trim().length in 1..120 && Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(time) &&
        Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(date) && runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(date) != null && date.length == 10 }.getOrDefault(false)

data class Progress(val done: Int, val total: Int) {
    val fraction: Float get() = if (total == 0) 0f else done.toFloat() / total
    val percent: Int get() = (fraction * 100).toInt()
}
fun List<TaskItem>.progress(date: String): Progress = filter { it.date == date }.let { Progress(it.count { t -> t.completed }, it.size) }
fun sampleTasks(date: String = today()) = listOf(
    TaskItem("sample-1", "Tập thể dục 30 phút", "Cuộc sống", "06:30", date, true),
    TaskItem("sample-2", "Nộp bài tập Lập trình di động", "Học tập", "09:00", date, true),
    TaskItem("sample-3", "Ôn chương 3 – Cấu trúc dữ liệu", "Học tập", "14:00", date),
    TaskItem("sample-4", "Đi chợ mua đồ cho tuần", "Cuộc sống", "17:30", date)
)
