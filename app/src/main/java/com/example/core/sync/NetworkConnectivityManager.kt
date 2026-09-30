package com.example.core.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkConnectivityManager(context: Context) {
  private val connectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  private val _isOnline = MutableStateFlow(checkCurrentNetwork())
  val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

  init {
    val request = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()

    connectivityManager.registerNetworkCallback(
      request,
      object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
          _isOnline.value = true
        }

        override fun onLost(network: Network) {
          _isOnline.value = checkCurrentNetwork()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
          val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
          _isOnline.value = hasInternet
        }
      }
    )
  }

  private fun checkCurrentNetwork(): Boolean {
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  companion object {
    @Volatile
    private var INSTANCE: NetworkConnectivityManager? = null

    fun getInstance(context: Context): NetworkConnectivityManager {
      return INSTANCE ?: synchronized(this) {
        val instance = NetworkConnectivityManager(context.applicationContext)
        INSTANCE = instance
        instance
      }
    }
  }
}
