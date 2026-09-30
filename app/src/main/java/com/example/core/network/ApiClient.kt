package com.example.core.network

import com.example.core.network.api.AnnouncementApiService
import com.example.core.network.api.DeviceApiService
import com.example.core.network.api.NotificationApiService
import com.example.core.network.api.TaskApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
  private var authToken: String? = null
  private var retrofit: Retrofit? = null
  private var announcementService: AnnouncementApiService? = null
  private var taskService: TaskApiService? = null
  private var notificationService: NotificationApiService? = null
  private var deviceService: DeviceApiService? = null

  fun setAuthToken(token: String?) {
    authToken = token
  }

  fun getAuthToken(): String? = authToken

  private val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  private val authInterceptor = Interceptor { chain ->
    val originalRequest = chain.request()
    val requestBuilder = originalRequest.newBuilder()

    authToken?.takeIf { it.isNotBlank() }?.let { token ->
      requestBuilder.header("Authorization", "Bearer $token")
    }

    requestBuilder.header("Accept", "application/json")
    requestBuilder.header("Content-Type", "application/json")

    chain.proceed(requestBuilder.build())
  }

  private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
  }

  private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .addInterceptor(loggingInterceptor)
    .connectTimeout(3, TimeUnit.SECONDS)
    .readTimeout(5, TimeUnit.SECONDS)
    .writeTimeout(5, TimeUnit.SECONDS)
    .build()

  @Synchronized
  fun rebuildRetrofit(baseUrl: String) {
    retrofit = Retrofit.Builder()
      .baseUrl(baseUrl)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
    announcementService = null
    taskService = null
    notificationService = null
    deviceService = null
  }

  @Synchronized
  private fun getRetrofit(): Retrofit {
    if (retrofit == null) {
      rebuildRetrofit(NetworkConfig.getBaseUrl())
    }
    return retrofit!!
  }

  @Synchronized
  fun getAnnouncementApiService(): AnnouncementApiService {
    if (announcementService == null) {
      announcementService = getRetrofit().create(AnnouncementApiService::class.java)
    }
    return announcementService!!
  }

  @Synchronized
  fun getTaskApiService(): TaskApiService {
    if (taskService == null) {
      taskService = getRetrofit().create(TaskApiService::class.java)
    }
    return taskService!!
  }

  @Synchronized
  fun getNotificationApiService(): NotificationApiService {
    if (notificationService == null) {
      notificationService = getRetrofit().create(NotificationApiService::class.java)
    }
    return notificationService!!
  }

  @Synchronized
  fun getDeviceApiService(): DeviceApiService {
    if (deviceService == null) {
      deviceService = getRetrofit().create(DeviceApiService::class.java)
    }
    return deviceService!!
  }
}
