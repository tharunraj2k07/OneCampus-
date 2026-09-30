package com.example.core.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SyncManager private constructor(private val context: Context) {
  private fun getWorkManager(): WorkManager? {
    return try {
      WorkManager.getInstance(context)
    } catch (e: Exception) {
      Log.w(TAG, "WorkManager instance not available: ${e.message}")
      null
    }
  }
  private val connectivityManager = NetworkConnectivityManager.getInstance(context)
  private val scope = CoroutineScope(Dispatchers.Default)

  fun init() {
    Log.d(TAG, "Initializing SyncManager periodic scheduling...")

    val wm = getWorkManager() ?: return

    val constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .build()

    val periodicSyncRequest = PeriodicWorkRequestBuilder<OfflineSyncWorker>(
      15, TimeUnit.MINUTES,
      5, TimeUnit.MINUTES // Flex interval
    )
      .setConstraints(constraints)
      .build()

    wm.enqueueUniquePeriodicWork(
      PERIODIC_SYNC_WORK_NAME,
      ExistingPeriodicWorkPolicy.KEEP,
      periodicSyncRequest
    )

    // Listen for network reconnection to trigger immediate sync
    scope.launch {
      var wasOnline = connectivityManager.isOnline.value
      connectivityManager.isOnline.collectLatest { isOnline ->
        if (isOnline && !wasOnline) {
          Log.d(TAG, "Network reconnected! Triggering immediate offline flush & sync...")
          syncNow()
        }
        wasOnline = isOnline
      }
    }
  }

  fun syncNow() {
    val wm = getWorkManager() ?: return
    val constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .build()

    val oneTimeRequest = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
      .setConstraints(constraints)
      .build()

    wm.enqueueUniqueWork(
      ONE_TIME_SYNC_WORK_NAME,
      ExistingWorkPolicy.REPLACE,
      oneTimeRequest
    )
  }

  companion object {
    private const val TAG = "SyncManager"
    private const val PERIODIC_SYNC_WORK_NAME = "onecampus_periodic_sync"
    private const val ONE_TIME_SYNC_WORK_NAME = "onecampus_one_time_sync"

    @Volatile
    private var INSTANCE: SyncManager? = null

    fun getInstance(context: Context): SyncManager {
      return INSTANCE ?: synchronized(this) {
        val instance = SyncManager(context.applicationContext)
        INSTANCE = instance
        instance
      }
    }
  }
}
