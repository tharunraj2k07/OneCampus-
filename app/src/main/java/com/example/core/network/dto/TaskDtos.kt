package com.example.core.network.dto

data class TaskResponseDto(
  val id: String = "",
  val studentId: String = "",
  val announcementId: String? = null,
  val actionIdentifier: String? = null,
  val title: String = "",
  val description: String? = null,
  val deadline: String? = null,
  val deadlineEpochMs: Long? = null,
  val priority: String = "MEDIUM",
  val status: String = "PENDING",
  val createdAt: String? = null,
  val completedAt: String? = null
)

data class TaskListResponseDto(
  val tasks: List<TaskResponseDto> = emptyList(),
  val total: Int = 0,
  val completedCount: Int = 0,
  val pendingCount: Int = 0,
  val page: Int = 1,
  val limit: Int = 20
)

data class TaskSingleResponseDto(
  val task: TaskResponseDto
)

data class UpdateTaskStatusRequestDto(
  val status: String
)

data class UpdateTaskRequestDto(
  val title: String? = null,
  val description: String? = null,
  val deadline: String? = null,
  val priority: String? = null
)

data class TaskSyncResponseDto(
  val createdCount: Int = 0
)
