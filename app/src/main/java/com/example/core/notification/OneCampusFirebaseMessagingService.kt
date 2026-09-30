package com.example.core.notification

import android.util.Log
import com.example.core.data.DeviceRepository
import com.example.core.database.OneCampusDatabase
import com.example.core.database.entities.NotificationEntity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class OneCampusFirebaseMessagingService : FirebaseMessagingService() {
  private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.d(TAG, "New FCM token generated: $token")
    serviceScope.launch {
      try {
        DeviceRepository.getInstance(applicationContext).registerToken(token)
      } catch (e: Exception) {
        Log.w(TAG, "Failed to register new FCM token: ${e.message}")
      }
    }
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

    val title = remoteMessage.notification?.title
      ?: remoteMessage.data["title"]
      ?: "OneCampus AI Notification"

    val body = remoteMessage.notification?.body
      ?: remoteMessage.data["body"]
      ?: "You have a new campus notification."

    val priority = remoteMessage.data["priority"] ?: "MEDIUM"
    val announcementId = remoteMessage.data["announcementId"]
    val taskId = remoteMessage.data["taskId"]
    val type = remoteMessage.data["type"] ?: "GENERAL"

    // 1. Cache in local Room database
    serviceScope.launch {
      try {
        val db = OneCampusDatabase.getInstance(applicationContext)
        val entity = NotificationEntity(
          id = UUID.randomUUID().toString(),
          userId = "",
          title = title,
          message = body,
          type = type,
          timestampEpochMs = System.currentTimeMillis(),
          isRead = false,
          announcementId = announcementId,
          taskId = taskId,
          priority = priority
        )
        db.notificationDao().insert(entity)
      } catch (e: Exception) {
        Log.w(TAG, "Error caching FCM notification in Room: ${e.message}")
      }
    }

    // 2. Display System Notification
    NotificationHelper.showNotification(
      context = applicationContext,
      title = title,
      body = body,
      priority = priority,
      announcementId = announcementId,
      taskId = taskId
    )
  }

  companion object {
    private const val TAG = "OneCampusFCM"
  }
}
