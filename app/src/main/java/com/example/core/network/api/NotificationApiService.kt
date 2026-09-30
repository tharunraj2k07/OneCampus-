package com.example.core.network.api

import com.example.core.network.dto.ApiResponseDto
import com.example.core.network.dto.NotificationListResponseDto
import com.example.core.network.dto.NotificationPreferencesDto
import com.example.core.network.dto.UpdateNotificationPreferencesRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApiService {
  @GET("api/v1/notifications")
  suspend fun getNotifications(
    @Query("page") page: Int = 1,
    @Query("limit") limit: Int = 30,
    @Query("unread") unreadOnly: Boolean = false
  ): Response<ApiResponseDto<NotificationListResponseDto>>

  @PATCH("api/v1/notifications/{id}/read")
  suspend fun markAsRead(
    @Path("id") id: String
  ): Response<ApiResponseDto<Any>>

  @PATCH("api/v1/notifications/read-all")
  suspend fun markAllAsRead(): Response<ApiResponseDto<Any>>

  @GET("api/v1/notifications/preferences")
  suspend fun getPreferences(): Response<ApiResponseDto<NotificationPreferencesDto>>

  @PUT("api/v1/notifications/preferences")
  suspend fun updatePreferences(
    @Body request: UpdateNotificationPreferencesRequestDto
  ): Response<ApiResponseDto<NotificationPreferencesDto>>
}
