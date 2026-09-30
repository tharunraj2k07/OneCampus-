package com.example.core.data

import android.content.Context
import android.os.Build
import com.example.core.network.ApiClient
import com.example.core.network.dto.RegisterDeviceTokenRequestDto
import com.example.core.sync.NetworkConnectivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceRepository(private val context: Context) {
  private val prefs = context.getSharedPreferences("onecampus_device_prefs", Context.MODE_PRIVATE)
  private val api = ApiClient.getDeviceApiService()
  private val connectivityManager = NetworkConnectivityManager.getInstance(context)

  fun getSavedToken(): String? {
    return prefs.getString("fcm_device_token", null)
  }

  suspend fun registerToken(token: String): Result<Unit> = withContext(Dispatchers.IO) {
    if (token.isBlank()) return@withContext Result.failure(IllegalArgumentException("Token cannot be blank"))

    prefs.edit().putString("fcm_device_token", token).apply()

    if (!connectivityManager.isOnline.value) {
      // Offline: token is saved and will be sent when back online
      return@withContext Result.success(Unit)
    }

    try {
      val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
      val response = api.registerToken(
        RegisterDeviceTokenRequestDto(
          token = token,
          platform = "ANDROID",
          deviceName = deviceName
        )
      )
      if (response.isSuccessful && response.body()?.success == true) {
        prefs.edit().putBoolean("fcm_token_synced", true).apply()
        Result.success(Unit)
      } else {
        Result.failure(Exception("Failed to register device token"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun unregisterToken(): Result<Unit> = withContext(Dispatchers.IO) {
    val token = getSavedToken() ?: return@withContext Result.success(Unit)
    try {
      if (connectivityManager.isOnline.value) {
        api.unregisterToken(token)
      }
      prefs.edit().remove("fcm_device_token").putBoolean("fcm_token_synced", false).apply()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  companion object {
    @Volatile
    private var INSTANCE: DeviceRepository? = null

    fun getInstance(context: Context): DeviceRepository {
      return INSTANCE ?: synchronized(this) {
        val instance = DeviceRepository(context.applicationContext)
        INSTANCE = instance
        instance
      }
    }
  }
}
