package com.rcsi.wellby
// manage notification about check in reminders or new messages from coaches
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.rcsi.wellby.R
import java.util.Calendar
import kotlin.random.Random

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            sendNotification(context)
            scheduleCheckInReminders(context, true)
        }
    }

    private fun sendNotification(context: Context) {
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java) as NotificationManager
        val builder = NotificationCompat.Builder(context, "checkin_channel")
            .setSmallIcon(R.drawable.wellby)  // Use actual icon
            .setContentTitle("Check-in time!")
            .setContentText("Please take a moment to check-in.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(Random.nextInt(), builder.build())
    }
}

fun scheduleCheckInReminders(context: Context, reschedule: Boolean = false) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    // Cancel previous alarms if rescheduling
    if (reschedule) {
        for (i in 1..2) {
            val cancelIntent = PendingIntent.getBroadcast(context, i, Intent(context, AlarmReceiver::class.java), flags)
            alarmManager.cancel(cancelIntent)
        }
    }

    val daysSet = mutableSetOf<Int>()
    // Set two new random alarms for the next week
    for (i in 1..2) {
        val calendar = Calendar.getInstance()
        var dayOfWeek: Int
        do {
            dayOfWeek = Random.nextInt(2, 7)  // Random day from Monday to Friday
        } while (!daysSet.add(dayOfWeek)) // Ensure different days

        val hourOfDay = Random.nextInt(15, 21) // Random hour from 3 PM to 9 PM
        val minute = Random.nextInt(60)

        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        // Ensure the alarm is set for the future
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, i, intent, flags)
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Call the method to reschedule your alarms
            scheduleCheckInReminders(context)
        }
    }
}

