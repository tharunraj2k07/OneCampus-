package com.example.core.network.api

import com.example.core.network.dto.AnnouncementFeedDataDto
import com.example.core.network.dto.AnnouncementResponseDto
import com.example.core.network.dto.ApiResponseDto
import com.example.core.network.dto.CreateAnnouncementRequestDto
import com.example.core.network.dto.DeleteAnnouncementResponseDto
import com.example.core.network.dto.UpdateAnnouncementRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AnnouncementApiService {

  @GET("api/v1/announcements/feed/personalized")
  suspend fun getPersonalizedFeed(
    @Query("category") category: String? = null,
    @Query("priority") priority: String? = null,
    @Query("search") search: String? = null,
    @Query("page") page: Int = 1,
    @Query("limit") limit: Int = 20
  ): Response<ApiResponseDto<AnnouncementFeedDataDto>>

  @GET("api/v1/announcements")
  suspend fun getFeedAnnouncements(
    @Query("category") category: String? = null,
    @Query("department") department: String? = null,
    @Query("year") year: Int? = null,
    @Query("search") search: String? = null,
    @Query("page") page: Int = 1,
    @Query("limit") limit: Int = 20
  ): Response<ApiResponseDto<AnnouncementFeedDataDto>>

  @GET("api/v1/announcements/my")
  suspend fun getMyAnnouncements(
    @Query("status") status: String? = null,
    @Query("page") page: Int = 1,
    @Query("limit") limit: Int = 50
  ): Response<ApiResponseDto<AnnouncementFeedDataDto>>

  @GET("api/v1/announcements/{id}")
  suspend fun getAnnouncementById(
    @Path("id") id: String
  ): Response<ApiResponseDto<AnnouncementResponseDto>>

  @GET("api/v1/announcements/{id}/preview")
  suspend fun previewAnnouncement(
    @Path("id") id: String
  ): Response<ApiResponseDto<AnnouncementResponseDto>>

  @POST("api/v1/announcements")
  suspend fun createAnnouncement(
    @Body request: CreateAnnouncementRequestDto
  ): Response<ApiResponseDto<AnnouncementResponseDto>>

  @POST("api/v1/announcements/{id}/publish")
  suspend fun publishAnnouncement(
    @Path("id") id: String
  ): Response<ApiResponseDto<AnnouncementResponseDto>>

  @POST("api/v1/announcements/{id}/analyze")
  suspend fun analyzeAnnouncement(
    @Path("id") id: String
  ): Response<ApiResponseDto<AnnouncementResponseDto>>

  @POST("api/v1/announcements/{id}/archive")
  suspend fun archiveAnnouncement(
    @Path("id") id: String
  ): Response<ApiResponseDto<AnnouncementResponseDto>>

  @PUT("api/v1/announcements/{id}")
  suspend fun updateAnnouncement(
    @Path("id") id: String,
    @Body request: UpdateAnnouncementRequestDto
  ): Response<ApiResponseDto<AnnouncementResponseDto>>

  @DELETE("api/v1/announcements/{id}")
  suspend fun deleteAnnouncement(
    @Path("id") id: String
  ): Response<ApiResponseDto<DeleteAnnouncementResponseDto>>

  @POST("api/v1/announcements/{id}/bookmark")
  suspend fun toggleBookmark(
    @Path("id") id: String
  ): Response<ApiResponseDto<Any>>
}
