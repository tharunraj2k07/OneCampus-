package com.example.core.data

import android.content.Context
import com.example.core.database.OneCampusDatabase
import com.example.core.database.entities.NotificationEntity
import com.example.core.database.entities.PendingSyncEntity
import com.example.core.model.NotificationItem
import com.example.core.model.PriorityLevel
import com.example.core.network.ApiClient
import com.example.core.network.dto.NotificationPreferencesDto
import com.example.core.network.dto.UpdateNotificationPreferencesRequestDto
import com.example.core.sync.NetworkConnectivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationRepository(private val context: Context) {
  private val db = OneCampusDatabase.getInstance(context)
  private val notificationDao = db.notificationDao()
  private val pendingSyncDao = db.pendingSyncDao()
  private val connectivityManager = NetworkConnectivityManager.getInstance(context)
  private val api = ApiClient.getNotificationApiService()

  val notifications: Flow<List<NotificationItem>> = notificationDao.getAll().map { list ->
    list.map { it.toDomain() }
  }

  val unreadCount: Flow<Int> = notificationDao.getUnreadCount()

  init {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val existing = notificationDao.getAll().first()
        if (existing.isEmpty()) {
          val initial = DemoRepository.notifications.value.map { item ->
            NotificationEntity(
              id = item.id,
              userId = "user_student_1",
              title = item.title,
              message = item.message,
              type = item.type,
              timestampEpochMs = item.timestampEpochMs,
              isRead = item.isRead,
              announcementId = item.announcementId,
              taskId = null,
              priority = PriorityLevel.MEDIUM.name
            )
          }
          notificationDao.insertAll(initial)
        }
      } catch (_: Exception) {}
    }
  }

  suspend fun refreshNotifications(): Result<Unit> = withContext(Dispatchers.IO) {
    if (!connectivityManager.isOnline.value) {
      return@withContext Result.success(Unit) // Rely on local cache when offline
    }

    try {
      val response = api.getNotifications(page = 1, limit = 50)
      if (response.isSuccessful && response.body()?.success == true) {
        val dtos = response.body()?.data?.notifications ?: emptyList()
        val entities = dtos.map { dto ->
          val prio = try {
            PriorityLevel.valueOf(dto.priority)
          } catch (_: Exception) {
            PriorityLevel.MEDIUM
          }
          val epochMs = try {
            if (dto.createdAt != null) {
              val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
              sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
              sdf.parse(dto.createdAt)?.time ?: System.currentTimeMillis()
            } else System.currentTimeMillis()
          } catch (_: Exception) {
            System.currentTimeMillis()
          }

          NotificationEntity(
            id = dto.id,
            userId = dto.userId,
            title = dto.title,
            message = dto.body,
            type = dto.type,
            timestampEpochMs = epochMs,
            isRead = dto.isRead,
            announcementId = dto.announcementId,
            taskId = dto.taskId,
            priority = prio.name
          )
        }
        notificationDao.insertAll(entities)
        Result.success(Unit)
      } else {
        Result.failure(Exception("Failed to fetch notifications: ${response.message()}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun markAsRead(id: String): Result<Unit> = withContext(Dispatchers.IO) {
    // 1. Optimistic local update
    notificationDao.markAsRead(id)

    // 2. Server sync or enqueue
    if (connectivityManager.isOnline.value) {
      try {
        api.markAsRead(id)
      } catch (_: Exception) {
        pendingSyncDao.insert(
          PendingSyncEntity(
            entityType = "NOTIFICATION",
            entityId = id,
            action = "MARK_READ",
            payloadJson = "{}"
          )
        )
      }
    } else {
      pendingSyncDao.insert(
        PendingSyncEntity(
          entityType = "NOTIFICATION",
          entityId = id,
          action = "MARK_READ",
          payloadJson = "{}"
        )
      )
    }
    Result.success(Unit)
  }

  suspend fun markAllAsRead(): Result<Unit> = withContext(Dispatchers.IO) {
    notificationDao.markAllAsRead()

    if (connectivityManager.isOnline.value) {
      try {
        api.markAllAsRead()
      } catch (_: Exception) {
        // Ignored, will refresh on next sync
      }
    }
    Result.success(Unit)
  }

  suspend fun getPreferences(): Result<NotificationPreferencesDto> = withContext(Dispatchers.IO) {
    try {
      val response = api.getPreferences()
      if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
        Result.success(response.body()!!.data!!)
      } else {
        Result.success(NotificationPreferencesDto())
      }
    } catch (e: Exception) {
      Result.success(NotificationPreferencesDto()) // Fallback default
    }
  }

  suspend fun updatePreferences(req: UpdateNotificationPreferencesRequestDto): Result<NotificationPreferencesDto> =
    withContext(Dispatchers.IO) {
      try {
        val response = api.updatePreferences(req)
        if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
          Result.success(response.body()!!.data!!)
        } else {
          Result.failure(Exception("Failed to update preferences"))
        }
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  companion object {
    @Volatile
    private var INSTANCE: NotificationRepository? = null

    fun getInstance(context: Context): NotificationRepository {
      return INSTANCE ?: synchronized(this) {
        val instance = NotificationRepository(context.applicationContext)
        INSTANCE = instance
        instance
      }
    }
  }
}
