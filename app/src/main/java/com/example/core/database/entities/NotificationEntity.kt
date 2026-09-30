package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.model.NotificationItem
import com.example.core.model.PriorityLevel

@Entity(
  tableName = "notifications",
  indices = [
    Index(value = ["timestampEpochMs"]),
    Index(value = ["isRead"])
  ]
)
data class NotificationEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val title: String,
  val message: String,
  val type: String,
  val timestampEpochMs: Long,
  val isRead: Boolean,
  val announcementId: String?,
  val taskId: String?,
  val priority: String
) {
  fun toDomain(): NotificationItem {
    val prio = try {
      PriorityLevel.valueOf(priority)
    } catch (_: Exception) {
      PriorityLevel.MEDIUM
    }

    return NotificationItem(
      id = id,
      title = title,
      message = message,
      type = type,
      timestampEpochMs = timestampEpochMs,
      isRead = isRead,
      announcementId = announcementId,
      taskId = taskId,
      priority = prio
    )
  }

  companion object {
    fun fromDomain(item: NotificationItem, userId: String = ""): NotificationEntity {
      return NotificationEntity(
        id = item.id,
        userId = userId,
        title = item.title,
        message = item.message,
        type = item.type,
        timestampEpochMs = item.timestampEpochMs,
        isRead = item.isRead,
        announcementId = item.announcementId,
        taskId = item.taskId,
        priority = item.priority.name
      )
    }
  }
}
