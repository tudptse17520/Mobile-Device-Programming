package com.example.uthsmarttasks

import com.example.uthsmarttasks.notifications.TaskReminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ReminderTimeTest {
    private val zone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")

    @Test fun remindersStayOnTheDueDateAndSkipPastTasks() {
        val lateYesterday = time(19, 23, 30)
        assertEquals(time(20, 8, 45), TaskReminder.reminderTime(time(20, 9, 0), lateYesterday, zone))
        assertEquals(time(20, 0, 0), TaskReminder.reminderTime(time(20, 0, 5), lateYesterday, zone))
        assertEquals(time(20, 8, 55), TaskReminder.reminderTime(time(20, 9, 0), time(20, 8, 55), zone))
        assertNull(TaskReminder.reminderTime(time(20, 9, 0), time(20, 9, 1), zone))
    }

    private fun time(day: Int, hour: Int, minute: Int) = Calendar.getInstance(zone).apply {
        clear()
        set(2026, Calendar.NOVEMBER, day, hour, minute)
    }.timeInMillis
}
