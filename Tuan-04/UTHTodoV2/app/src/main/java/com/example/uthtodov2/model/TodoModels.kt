package com.example.uthtodov2.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class TaskCategory(val displayName: String, val badgeColor: Long, val badgeBgColor: Long) {
    STUDY("Học tập", 0xFF2F5BEA, 0xFFEBF0FF),
    LIFE("Cuộc sống", 0xFFE65100, 0xFFFFF3E0);

    companion object {
        fun fromDisplayName(name: String): TaskCategory {
            return entries.find { it.displayName.equals(name, ignoreCase = true) } ?: STUDY
        }
    }
}

class TodoTask(
    val id: String,
    var title: String,
    val category: TaskCategory,
    initialCompleted: Boolean = false,
    var time: String = "Hôm nay, 09:00"
) {
    var isCompleted by mutableStateOf(initialCompleted)
        private set

    fun toggleCompleted() {
        isCompleted = !isCompleted
    }

    fun markCompleted() {
        isCompleted = true
    }

    fun markIncomplete() {
        isCompleted = false
    }
}

data class OnboardingPage(
    val step: Int,
    val title: String,
    val description: String,
    val buttonText: String
)
