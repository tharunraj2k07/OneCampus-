package com.example.core.data

import android.util.Log
import com.example.OneCampusApp
import com.example.core.database.OneCampusDatabase
import com.example.core.database.entities.PendingSyncEntity
import com.example.core.database.entities.TaskEntity
import com.example.core.model.PriorityLevel
import com.example.core.model.TaskItem
import com.example.core.network.ApiClient
import com.example.core.network.dto.TaskResponseDto
import com.example.core.network.dto.UpdateTaskStatusRequestDto
import com.example.core.sync.NetworkConnectivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TaskRepository private constructor() {

  companion object {
    private const val TAG = "TaskRepository"

    @Volatile
    private var instance: TaskRepository? = null

    fun getInstance(): TaskRepository {
      return instance ?: synchronized(this) {
        instance ?: TaskRepository().also { instance = it }
      }
    }
  }

  private val db by lazy { OneCampusDatabase.getInstance(OneCampusApp.getAppContext()) }
  private val connectivityManager by lazy { NetworkConnectivityManager.getInstance(OneCampusApp.getAppContext()) }

  private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
  val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  init {
    // Populate initial state from DemoRepository to guarantee instant display
    _tasks.value = DemoRepository.tasks.value

    // Load cached tasks from Room on background thread, or warm cache if empty
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val cached = db.taskDao().getAll().first()
        if (cached.isNotEmpty()) {
          val domainTasks = cached.map { it.toDomain() }
          _tasks.value = domainTasks
        } else {
          val initial = DemoRepository.tasks.value
          db.taskDao().insertAll(initial.map { TaskEntity.fromDomain(it) })
        }
      } catch (_: Exception) {}
    }
  }

  private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
  }

  private fun parseDateToEpochMs(dateStr: String?): Long? {
    if (dateStr.isNullOrBlank()) return null
    return try {
      isoDateFormat.parse(dateStr)?.time
    } catch (_: Exception) {
      null
    }
  }

  private fun dtoToTaskItem(dto: TaskResponseDto): TaskItem {
    val deadlineEpoch = dto.deadlineEpochMs ?: parseDateToEpochMs(dto.deadline)
    val priority = when (dto.priority.uppercase()) {
      "CRITICAL" -> PriorityLevel.CRITICAL
      "HIGH" -> PriorityLevel.HIGH
      "MEDIUM" -> PriorityLevel.MEDIUM
      else -> PriorityLevel.LOW
    }

    val isCompleted = dto.status.uppercase() == "COMPLETED"

    return TaskItem(
      id = dto.id,
      announcementId = dto.announcementId,
      title = dto.title,
      description = dto.description,
      category = "Action Required",
      deadlineEpochMs = deadlineEpoch,
      priority = priority,
      isCompleted = isCompleted,
      status = dto.status,
      actionIdentifier = dto.actionIdentifier,
      requiredAction = dto.title
    )
  }

  /**
   * Fetches tasks from the backend task API.
   * Scoped strictly to the authenticated student.
   */
  suspend fun refreshTasks(status: String? = null): Result<List<TaskItem>> = withContext(Dispatchers.IO) {
    _isLoading.value = true
    _errorMessage.value = null
    try {
      val response = ApiClient.getTaskApiService().getTasks(
        status = status,
        limit = 50
      )

      if (response.isSuccessful && response.body()?.data != null) {
        val remoteTasks = response.body()!!.data!!.tasks.map { dtoToTaskItem(it) }
        _tasks.value = remoteTasks
        _isLoading.value = false
        // Persist to Room
        try {
          db.taskDao().insertAll(remoteTasks.map { TaskEntity.fromDomain(it) })
        } catch (_: Exception) {}
        Result.success(remoteTasks)
      } else {
        Log.w(TAG, "Failed to load tasks from server, checking Room cache")
        _isLoading.value = false
        try {
          val cached = db.taskDao().getAll().first()
          if (cached.isNotEmpty()) {
            val domainTasks = cached.map { it.toDomain() }
            _tasks.value = domainTasks
            return@withContext Result.success(domainTasks)
          }
        } catch (_: Exception) {}
        Result.success(_tasks.value)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Backend unreachable for tasks (${e.javaClass.simpleName}: ${e.message}), loading Room cache")
      _isLoading.value = false
      try {
        val cached = db.taskDao().getAll().first()
        if (cached.isNotEmpty()) {
          val domainTasks = cached.map { it.toDomain() }
          _tasks.value = domainTasks
          return@withContext Result.success(domainTasks)
        }
      } catch (_: Exception) {}
      Result.success(_tasks.value)
    }
  }

  /**
   * Toggles task completion status (PENDING <-> COMPLETED)
   * Optimistically updates local StateFlow and Room, queues for sync if offline.
   */
  suspend fun toggleTask(taskId: String): Result<TaskItem> = withContext(Dispatchers.IO) {
    val currentTask = _tasks.value.find { it.id == taskId }
    if (currentTask == null) {
      // Check DemoRepository as fallback
      DemoRepository.toggleTaskCompletion(taskId)
      val updatedDemo = DemoRepository.tasks.value.find { it.id == taskId }
      if (updatedDemo != null) {
        _tasks.update { list ->
          if (list.any { it.id == taskId }) {
            list.map { if (it.id == taskId) updatedDemo else it }
          } else {
            list + updatedDemo
          }
        }
        return@withContext Result.success(updatedDemo)
      }
      return@withContext Result.failure(Exception("Task not found"))
    }

    val newCompleted = !currentTask.isCompleted
    val newStatus = if (newCompleted) "COMPLETED" else "PENDING"
    val updated = currentTask.copy(
      isCompleted = newCompleted,
      status = newStatus
    )

    // Optimistic local update
    _tasks.update { list -> list.map { if (it.id == taskId) updated else it } }
    DemoRepository.toggleTaskCompletion(taskId)

    val isOnline = connectivityManager.isOnline.value
    // Update local Room database immediately (Last Write Wins)
    try {
      db.taskDao().updateTaskStatus(
        id = taskId,
        status = newStatus,
        isCompleted = newCompleted,
        syncStatus = if (isOnline) "SYNCED" else "PENDING_UPDATE",
        updatedAt = System.currentTimeMillis()
      )
    } catch (_: Exception) {}

    if (isOnline) {
      try {
        val response = ApiClient.getTaskApiService().updateTaskStatus(
          id = taskId,
          request = UpdateTaskStatusRequestDto(status = newStatus)
        )
        if (response.isSuccessful && response.body()?.data?.task != null) {
          val serverTask = dtoToTaskItem(response.body()!!.data!!.task)
          _tasks.update { list -> list.map { if (it.id == taskId) serverTask else it } }
          Result.success(serverTask)
        } else {
          Result.success(updated)
        }
      } catch (e: Exception) {
        Log.w(TAG, "Network error updating task status, queueing for background sync", e)
        db.pendingSyncDao().insert(
          PendingSyncEntity(
            entityType = "TASK",
            entityId = taskId,
            action = "UPDATE_STATUS",
            payloadJson = "\"$newStatus\""
          )
        )
        Result.success(updated)
      }
    } else {
      // Offline: enqueue in Room pending sync queue
      try {
        db.pendingSyncDao().insert(
          PendingSyncEntity(
            entityType = "TASK",
            entityId = taskId,
            action = "UPDATE_STATUS",
            payloadJson = "\"$newStatus\""
          )
        )
      } catch (_: Exception) {}
      Result.success(updated)
    }
  }

  /**
   * Triggers task synchronization on the backend
   */
  suspend fun syncTasks(): Result<Int> = withContext(Dispatchers.IO) {
    try {
      val response = ApiClient.getTaskApiService().syncTasks()
      if (response.isSuccessful && response.body()?.data != null) {
        val count = response.body()!!.data!!.createdCount
        refreshTasks()
        Result.success(count)
      } else {
        Result.success(0)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Backend unreachable during task sync: ${e.message}")
      Result.failure(e)
    }
  }
}
