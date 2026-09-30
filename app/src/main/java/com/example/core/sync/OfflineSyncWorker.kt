package com.example.core.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.data.AnnouncementRepository
import com.example.core.data.DeviceRepository
import com.example.core.data.NotificationRepository
import com.example.core.data.TaskRepository
import com.example.core.database.OneCampusDatabase
import com.example.core.network.ApiClient
import com.example.core.network.dto.UpdateTaskStatusRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfflineSyncWorker(
  appContext: Context,
  workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    Log.d(TAG, "OfflineSyncWorker starting synchronization...")

    val db = OneCampusDatabase.getInstance(applicationContext)
    val pendingSyncDao = db.pendingSyncDao()
    val taskDao = db.taskDao()

    try {
      // 1. Flush Pending Sync Queue (Offline Mutations)
      val pendingOperations = pendingSyncDao.getAll()
      for (op in pendingOperations) {
        try {
          when (op.action) {
            "UPDATE_STATUS" -> {
              // Task status update
              val status = op.payloadJson.replace("\"", "").trim()
              val api = ApiClient.getTaskApiService()
              val res = api.updateTaskStatus(op.entityId, UpdateTaskStatusRequestDto(status = status))
              if (res.isSuccessful) {
                pendingSyncDao.deleteById(op.operationId)
              } else {
                pendingSyncDao.incrementRetry(op.operationId)
              }
            }
            "TOGGLE_BOOKMARK" -> {
              val api = ApiClient.getAnnouncementApiService()
              val res = api.toggleBookmark(op.entityId)
              if (res.isSuccessful) {
                pendingSyncDao.deleteById(op.operationId)
              } else {
                pendingSyncDao.incrementRetry(op.operationId)
              }
            }
            "MARK_READ" -> {
              val api = ApiClient.getNotificationApiService()
              val res = api.markAsRead(op.entityId)
              if (res.isSuccessful) {
                pendingSyncDao.deleteById(op.operationId)
              } else {
                pendingSyncDao.incrementRetry(op.operationId)
              }
            }
          }
        } catch (e: Exception) {
          Log.w(TAG, "Failed syncing operation ${op.operationId}: ${e.message}")
          pendingSyncDao.incrementRetry(op.operationId)
        }
      }

      // 2. Check pending sync tasks
      val pendingTasks = taskDao.getPendingSyncTasks()
      for (pt in pendingTasks) {
        try {
          val res = ApiClient.getTaskApiService().updateTaskStatus(
            pt.id,
            UpdateTaskStatusRequestDto(status = pt.status)
          )
          if (res.isSuccessful) {
            taskDao.updateTaskStatus(
              id = pt.id,
              status = pt.status,
              isCompleted = pt.isCompleted,
              syncStatus = "SYNCED",
              updatedAt = pt.updatedAtEpochMs
            )
          }
        } catch (_: Exception) {}
      }

      // 3. Register device token if pending
      val deviceRepo = DeviceRepository.getInstance(applicationContext)
      deviceRepo.getSavedToken()?.let { token ->
        deviceRepo.registerToken(token)
      }

      // 4. Refresh Announcements, Tasks, and Notifications
      AnnouncementRepository.getInstance().refreshPersonalizedFeed()
      TaskRepository.getInstance().refreshTasks()
      NotificationRepository.getInstance(applicationContext).refreshNotifications()

      Log.d(TAG, "OfflineSyncWorker synchronization finished successfully.")
      Result.success()
    } catch (e: Exception) {
      Log.w(TAG, "OfflineSyncWorker completed with network condition: ${e.message}")
      Result.success()
    }
  }

  companion object {
    private const val TAG = "OfflineSyncWorker"
  }
}
