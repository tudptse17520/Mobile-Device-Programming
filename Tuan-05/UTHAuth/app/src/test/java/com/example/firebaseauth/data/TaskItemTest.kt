package com.example.firebaseauth.data

import org.junit.Assert.*
import org.junit.Test

class TaskItemTest {
    @Test fun progressOnlyIncludesSelectedDay() {
        val tasks = sampleTasks("2026-11-03") + TaskItem(date = "2026-11-04", completed = true)
        assertEquals(Progress(2, 4), tasks.progress("2026-11-03"))
        assertEquals(50, tasks.progress("2026-11-03").percent)
        assertEquals(0f, emptyList<TaskItem>().progress("2026-11-03").fraction)
    }
    @Test fun completingTaskUpdatesProgressAndRemainingCategory() {
        val tasks = sampleTasks("2026-11-03").map { if (it.id == "sample-3") it.copy(completed = true) else it }
        assertEquals(75, tasks.progress("2026-11-03").percent)
        assertEquals(0, tasks.count { it.category == "Học tập" && !it.completed })
        assertEquals(1, tasks.count { it.category == "Cuộc sống" && !it.completed })
    }
    @Test fun editorRejectsInvalidDatesTimesAndBlankTitles() {
        assertTrue(validTask("Nộp bài", "23:59", "2028-02-29"))
        assertFalse(validTask(" ", "09:00", "2026-11-03"))
        assertFalse(validTask("Nộp bài", "24:00", "2026-11-03"))
        assertFalse(validTask("Nộp bài", "9:00", "2026-11-03"))
        assertFalse(validTask("Nộp bài", "09:00", "2026-02-29"))
        assertFalse(validTask("Nộp bài", "09:00", "2026-11-31"))
    }
}
