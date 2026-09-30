package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.model.PriorityLevel
import com.example.core.model.TaskItem

@Entity(
  tableName = "tasks",
  indices = [
    Index(value = ["isCompleted", "deadlineEpochMs"]),
    Index(value = ["syncStatus"]),
    Index(value = ["announcementId"])
  ]
)
data class TaskEntity(
  @PrimaryKey val id: String,
  val announcementId: String?,
  val title: String,
  val description: String?,
  val category: String,
  val deadlineEpochMs: Long?,
  val priority: String,
  val isCompleted: Boolean,
  val status: String,
  val actionIdentifier: String?,
  val requiredAction: String,
  val syncStatus: String = "SYNCED", // SYNCED, PENDING_UPDATE
  val updatedAtEpochMs: Long = System.currentTimeMillis()
) {
  fun toDomain(): TaskItem {
    val prio = try {
      PriorityLevel.valueOf(priority)
    } catch (_: Exception) {
      PriorityLevel.MEDIUM
    }

    return TaskItem(
      id = id,
      announcementId = announcementId,
      title = title,
      description = description,
      category = category,
      deadlineEpochMs = deadlineEpochMs,
      priority = prio,
      isCompleted = isCompleted,
      status = status,
      actionIdentifier = actionIdentifier,
      requiredAction = requiredAction
    )
  }

  companion object {
    fun fromDomain(task: TaskItem, syncStatus: String = "SYNCED", updatedAt: Long = System.currentTimeMillis()): TaskEntity {
      return TaskEntity(
        id = task.id,
        announcementId = task.announcementId,
        title = task.title,
        description = task.description,
        category = task.category,
        deadlineEpochMs = task.deadlineEpochMs,
        priority = task.priority.name,
        isCompleted = task.isCompleted,
        status = task.status,
        actionIdentifier = task.actionIdentifier,
        requiredAction = task.requiredAction,
        syncStatus = syncStatus,
        updatedAtEpochMs = updatedAt
      )
    }
  }
}
