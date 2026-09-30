package com.example.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NetworkConfig {
  // Default base URL for Android Emulator pointing to Node.js backend on host machine
  private const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:5000/"
  private const val DEFAULT_DEVICE_URL = "http://localhost:5000/"

  private val _baseUrl = MutableStateFlow(DEFAULT_EMULATOR_URL)
  val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

  fun getBaseUrl(): String = _baseUrl.value

  fun setBaseUrl(newUrl: String) {
    val formattedUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
    _baseUrl.value = formattedUrl
    ApiClient.rebuildRetrofit(formattedUrl)
  }

  fun resetToDefault() {
    setBaseUrl(DEFAULT_EMULATOR_URL)
  }
}
