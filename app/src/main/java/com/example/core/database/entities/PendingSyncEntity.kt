package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pending_sync_queue")
data class PendingSyncEntity(
  @PrimaryKey val operationId: String = UUID.randomUUID().toString(),
  val entityType: String, // "TASK", "BOOKMARK", "PROFILE"
  val entityId: String,
  val action: String, // "UPDATE_STATUS", "TOGGLE_BOOKMARK", "UPDATE_PREFERENCES"
  val payloadJson: String,
  val timestampEpochMs: Long = System.currentTimeMillis(),
  val retryCount: Int = 0
)
