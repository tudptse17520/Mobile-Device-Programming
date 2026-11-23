package com.example.uthsmarttasks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NotificationKind { REMINDER, OVERDUE, COMPLETED }

data class NotificationItem(
    val key: String,
    val kind: NotificationKind,
    val title: String,
    val summary: String,
    val timeLabel: String,
    val unread: Boolean = false
)

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications = _notifications.asStateFlow()
    private var clearDemo: Job? = null

    fun startDemoIfNeeded() {
        if (clearDemo == null) startDemo()
    }

    fun startDemo() {
        clearDemo?.cancel()
        _notifications.value = listOf(
            NotificationItem("due", NotificationKind.REMINDER, "Sắp đến hạn",
                "Ôn chương 3 – Cấu trúc dữ liệu lúc 14:00.", "13:45", unread = true),
            NotificationItem("overdue", NotificationKind.OVERDUE, "Đã quá hạn",
                "Bài đọc tham khảo môn Mạng máy tính chưa xong.", "10:10", unread = true),
            NotificationItem("done", NotificationKind.COMPLETED, "Làm tốt lắm!",
                "Bạn đã xong 2 việc trong sáng nay.", "11:30"),
            NotificationItem("reminder", NotificationKind.REMINDER, "Nhắc việc",
                "Họp nhóm đồ án trong tuần này.", "Hôm qua")
        )
        clearDemo = viewModelScope.launch {
            delay(10_000)
            _notifications.value = emptyList()
        }
    }

    fun markRead(keys: Set<String>) {
        _notifications.update { items ->
            items.map { if (it.key in keys) it.copy(unread = false) else it }
        }
    }
}
