package com.example.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {
  const val CHANNEL_CRITICAL = "onecampus_critical"
  const val CHANNEL_DEADLINES = "onecampus_deadlines"
  const val CHANNEL_GENERAL = "onecampus_general"

  fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      // 1. Critical Channel
      val criticalChannel = NotificationChannel(
        CHANNEL_CRITICAL,
        "Critical Alerts",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Emergency, academic deadlines, and critical institutional notices"
        enableLights(true)
        lightColor = Color.RED
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 300, 200, 300)
      }

      // 2. Deadlines Channel
      val deadlineChannel = NotificationChannel(
        CHANNEL_DEADLINES,
        "Deadline Reminders",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Upcoming assignment, exam, and registration reminders"
        enableLights(true)
        lightColor = Color.YELLOW
        enableVibration(true)
      }

      // 3. General Announcements
      val generalChannel = NotificationChannel(
        CHANNEL_GENERAL,
        "General Announcements",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Events, clubs, workshops, and general campus updates"
      }

      notificationManager.createNotificationChannels(
        listOf(criticalChannel, deadlineChannel, generalChannel)
      )
    }
  }

  fun showNotification(
    context: Context,
    title: String,
    body: String,
    priority: String = "MEDIUM",
    announcementId: String? = null,
    taskId: String? = null
  ) {
    val channelId = when (priority.uppercase()) {
      "CRITICAL" -> CHANNEL_CRITICAL
      else -> if (taskId != null) CHANNEL_DEADLINES else CHANNEL_GENERAL
    }

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      if (!announcementId.isNullOrBlank()) putExtra("announcement_id", announcementId)
      if (!taskId.isNullOrBlank()) putExtra("task_id", taskId)
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      System.currentTimeMillis().toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle(title)
      .setContentText(body)
      .setStyle(NotificationCompat.BigTextStyle().bigText(body))
      .setAutoCancel(true)
      .setSound(defaultSoundUri)
      .setContentIntent(pendingIntent)
      .setPriority(
        if (priority.uppercase() == "CRITICAL") NotificationCompat.PRIORITY_HIGH
        else NotificationCompat.PRIORITY_DEFAULT
      )

    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationId = (System.currentTimeMillis() % 100000).toInt()
    notificationManager.notify(notificationId, notificationBuilder.build())
  }
}
