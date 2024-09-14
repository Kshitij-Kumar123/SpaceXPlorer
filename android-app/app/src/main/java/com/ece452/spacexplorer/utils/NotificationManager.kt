package com.ece452.spacexplorer.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log
import android.app.AlarmManager
import android.app.PendingIntent
import com.ece452.spacexplorer.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationManager : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val nId = intent?.getIntExtra("nId", 0) ?: 1
        val title = intent?.getStringExtra("Desc") ?: "Undetermined Event"
        val time = intent?.getStringExtra("Time") ?: "Undetermined Time"

        context?.let {
            val builder = NotificationCompat.Builder(it, "CHANNEL_ID")
                .setSmallIcon(R.mipmap.app_icon)
                .setContentTitle(title)
                .setContentText("This event occurs at: $time")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(it)) {
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("NotificationManager", "Notification Permissions Denied") // This should not fire, should be handled earlier
                }
                notify(nId, builder.build())
            }
        }
    }

    fun scheduleNotification(context: Context, time: String, nId: Int, eventDesc: String) {
        val intent = Intent(context, this::class.java).apply {
            putExtra("nId", nId)
            putExtra("Desc", eventDesc)
            putExtra("Time", time)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationFireIn = convertTimeToMillis(time)

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            notificationFireIn,
            pendingIntent
        )
    }

    private fun convertTimeToMillis(time: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val dateOfEvent: Date = format.parse(time)
        val timeUntilEvent = dateOfEvent.time
        return timeUntilEvent
    }
}