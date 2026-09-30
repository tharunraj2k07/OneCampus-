package com.example.core.network.dto

data class RegisterDeviceTokenRequestDto(
  val token: String,
  val platform: String = "ANDROID",
  val deviceName: String? = null
)

data class NotificationResponseDto(
  val id: String = "",
  val userId: String = "",
  val title: String = "",
  val body: String = "",
  val type: String = "GENERAL",
  val announcementId: String? = null,
  val taskId: String? = null,
  val category: String = "GENERAL",
  val priority: String = "MEDIUM",
  val isRead: Boolean = false,
  val createdAt: String? = null
)

data class NotificationListResponseDto(
  val notifications: List<NotificationResponseDto> = emptyList(),
  val pagination: PaginationDto? = null,
  val unreadCount: Int = 0
)

data class NotificationPreferencesDto(
  val pushEnabled: Boolean = true,
  val criticalAlerts: Boolean = true,
  val placementAlerts: Boolean = true,
  val assignmentAlerts: Boolean = true,
  val eventAlerts: Boolean = true,
  val deadlineReminders: Boolean = true
)

data class UpdateNotificationPreferencesRequestDto(
  val pushEnabled: Boolean? = null,
  val criticalAlerts: Boolean? = null,
  val placementAlerts: Boolean? = null,
  val assignmentAlerts: Boolean? = null,
  val eventAlerts: Boolean? = null,
  val deadlineReminders: Boolean? = null
)
