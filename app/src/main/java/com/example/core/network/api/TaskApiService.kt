package com.example.core.network.api

import com.example.core.network.dto.ApiResponseDto
import com.example.core.network.dto.TaskListResponseDto
import com.example.core.network.dto.TaskResponseDto
import com.example.core.network.dto.TaskSingleResponseDto
import com.example.core.network.dto.TaskSyncResponseDto
import com.example.core.network.dto.UpdateTaskRequestDto
import com.example.core.network.dto.UpdateTaskStatusRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskApiService {

  @GET("api/v1/tasks")
  suspend fun getTasks(
    @Query("status") status: String? = null,
    @Query("announcementId") announcementId: String? = null,
    @Query("page") page: Int = 1,
    @Query("limit") limit: Int = 50
  ): Response<ApiResponseDto<TaskListResponseDto>>

  @GET("api/v1/tasks/{id}")
  suspend fun getTaskById(
    @Path("id") id: String
  ): Response<ApiResponseDto<TaskSingleResponseDto>>

  @PUT("api/v1/tasks/{id}")
  suspend fun updateTask(
    @Path("id") id: String,
    @Body request: UpdateTaskRequestDto
  ): Response<ApiResponseDto<TaskSingleResponseDto>>

  @PATCH("api/v1/tasks/{id}/status")
  suspend fun updateTaskStatus(
    @Path("id") id: String,
    @Body request: UpdateTaskStatusRequestDto
  ): Response<ApiResponseDto<TaskSingleResponseDto>>

  @POST("api/v1/tasks/sync")
  suspend fun syncTasks(): Response<ApiResponseDto<TaskSyncResponseDto>>
}
