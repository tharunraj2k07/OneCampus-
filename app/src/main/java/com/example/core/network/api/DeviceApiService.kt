package com.example.core.network.api

import com.example.core.network.dto.ApiResponseDto
import com.example.core.network.dto.RegisterDeviceTokenRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface DeviceApiService {
  @POST("api/v1/devices/token")
  suspend fun registerToken(
    @Body request: RegisterDeviceTokenRequestDto
  ): Response<ApiResponseDto<Any>>

  @DELETE("api/v1/devices/token/{token}")
  suspend fun unregisterToken(
    @Path("token") token: String
  ): Response<ApiResponseDto<Any>>
}
