package com.example.uthsmarttasks.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.uthsmarttasks.MainActivity
import com.example.uthsmarttasks.R
import com.example.uthsmarttasks.model.Task
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TaskReminder {
    private const val CHANNEL_WITH_SOUND = "task_reminders_sound"
    private const val CHANNEL_SILENT = "task_reminders_silent"
    private const val DAILY_SUMMARY_ID = Int.MIN_VALUE
    private const val PLAN_KEY = "scheduled_tasks"
    private const val OWNER_KEY = "account"
    private val gson = Gson()

    private fun planPrefs(context: Context) = context.getSharedPreferences("uth_reminder_plan", Context.MODE_PRIVATE)
    private fun settings(context: Context) = context.getSharedPreferences("uth_todo_settings", Context.MODE_PRIVATE)

    private data class Reminder(val id: Int, val title: String, val date: String, val time: String)

    private fun plan(context: Context): List<Reminder> = runCatching {
        gson.fromJson(planPrefs(context).getString(PLAN_KEY, "[]"), Array<Reminder>::class.java).orEmpty().toList()
    }.getOrDefault(emptyList())

    fun useAccount(context: Context, uid: String?) {
        val prefs = planPrefs(context)
        if (prefs.getString(OWNER_KEY, null) == uid) return
        sync(context, emptyList(), false)
        cancel(context, DAILY_SUMMARY_ID)
        prefs.edit().clear().putString(OWNER_KEY, uid).apply()
        NotificationManagerCompat.from(context).cancelAll()
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(CHANNEL_WITH_SOUND, "Nhắc việc có âm thanh", NotificationManager.IMPORTANCE_DEFAULT))
            val silentChannel = NotificationChannel(CHANNEL_SILENT, "Nhắc việc im lặng", NotificationManager.IMPORTANCE_DEFAULT).apply {
                setSound(null, null)
                enableVibration(false)
            }
            manager.createNotificationChannel(silentChannel)
        }
    }

    fun scheduleDailySummary(context: Context, enabled: Boolean) {
        cancel(context, DAILY_SUMMARY_ID)
        if (!enabled) return
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        setAlarm(context, calendar.timeInMillis, pendingIntent(context, DAILY_SUMMARY_ID, "", "", "21:00"))
    }

    fun sync(context: Context, tasks: List<Task>, enabled: Boolean) {
        val prefs = planPrefs(context)
        val now = System.currentTimeMillis()
        val previous = plan(context)
        val next = if (!enabled) emptyList() else tasks
            .filter { it.remind && !it.done }
            .map { Reminder(it.id, it.title, it.date, it.time) }
            .filter { (dueAt(it.date, it.time) ?: 0) > now }
        val nextIds = next.mapTo(mutableSetOf()) { it.id }
        previous.filterNot { it.id in nextIds }.forEach { cancel(context, it.id) }
        prefs.edit().putString(PLAN_KEY, gson.toJson(next)).apply()
        next.filter { it !in previous }.forEach { reminder ->
            prefs.edit().remove("delivered_${reminder.id}").apply()
            schedule(context, reminder)
        }
    }

    fun restore(context: Context) {
        val settings = settings(context)
        if (settings.getBoolean("reminders", true)) {
            plan(context).forEach { schedule(context, it) }
        }
        scheduleDailySummary(context, settings.getBoolean("daily_summary", false))
    }

    fun canScheduleExactly(context: Context): Boolean =
        Build.VERSION.SDK_INT < 31 || context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    private fun dueAt(date: String, time: String): Long? {
        if (!date.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")) ||
            !time.matches(Regex("([01][0-9]|2[0-3]):[0-5][0-9]"))) return null
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply { isLenient = false }
            .let { runCatching { it.parse("$date $time")?.time }.getOrNull() }
    }

    private fun schedule(context: Context, reminder: Reminder) {
        val due = dueAt(reminder.date, reminder.time) ?: return
        val now = System.currentTimeMillis()
        if (wasDelivered(context, reminder)) return
        val remindAt = reminderTime(due, now) ?: return
        setAlarm(context, remindAt, pendingIntent(context, reminder.id, reminder.title, reminder.date, reminder.time))
    }

    internal fun reminderTime(due: Long, now: Long, zone: java.util.TimeZone = java.util.TimeZone.getDefault()): Long? {
        if (due <= now) return null
        val startOfDay = Calendar.getInstance(zone).apply {
            timeInMillis = due
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return maxOf(now, startOfDay, due - 15 * 60 * 1000)
    }

    private fun setAlarm(context: Context, at: Long, pending: PendingIntent) {
        val manager = context.getSystemService(AlarmManager::class.java)
        if (canScheduleExactly(context)) {
            try {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
                return
            } catch (_: SecurityException) {
                // Quyền có thể bị thu hồi ngay sau khi kiểm tra.
            }
        }
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
    }

    fun cancel(context: Context, id: Int) {
        val pending = pendingIntent(context, id, "", "", "")
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
        pending.cancel()
        if (id != DAILY_SUMMARY_ID) {
            planPrefs(context).edit().remove("delivered_$id").apply()
            NotificationManagerCompat.from(context).cancel(id)
        }
    }

    private fun pendingIntent(context: Context, id: Int, title: String, date: String, time: String): PendingIntent {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = "com.example.uthsmarttasks.REMINDER_$id"
            putExtra("task_id", id)
            putExtra("task_title", title)
            putExtra("task_date", date)
            putExtra("task_time", time)
        }
        return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun wasDelivered(context: Context, reminder: Reminder) =
        planPrefs(context).getString("delivered_${reminder.id}", null) == "${reminder.date} ${reminder.time}"

    internal fun deliver(context: Context, id: Int, title: String, date: String, time: String) {
        val settings = settings(context)
        val summary = id == DAILY_SUMMARY_ID
        if (summary) {
            if (!settings.getBoolean("daily_summary", false)) return
            scheduleDailySummary(context, true)
        } else {
            val reminder = Reminder(id, title, date, time)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(System.currentTimeMillis())
            if (!settings.getBoolean("reminders", true) || date != today || reminder !in plan(context) ||
                wasDelivered(context, reminder)) return
        }
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        createChannel(context)
        val soundEnabled = settings.getBoolean("notification_sound", true)
        val channelId = if (soundEnabled) CHANNEL_WITH_SOUND else CHANNEL_SILENT
        val isDailySummary = id == DAILY_SUMMARY_ID
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_figma_notifications)
            .setContentTitle(if (isDailySummary) "Tổng kết cuối ngày" else "Đến giờ làm việc")
            .setContentText(if (isDailySummary) "Đừng quên kiểm tra các công việc còn lại hôm nay." else "$title · Hạn chót lúc $time")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
        if (!summary) planPrefs(context).edit().putString("delivered_$id", "$date $time").apply()
    }
}

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!intent.hasExtra("task_id")) return
        TaskReminder.deliver(context, intent.getIntExtra("task_id", 0), intent.getStringExtra("task_title").orEmpty(),
            intent.getStringExtra("task_date").orEmpty(), intent.getStringExtra("task_time").orEmpty())
    }
}

class RestoreRemindersReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = TaskReminder.restore(context)
}
