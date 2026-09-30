package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.core.data.DeviceRepository
import com.example.core.database.OneCampusDatabase
import com.example.core.notification.NotificationHelper
import com.example.core.sync.SyncManager
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OneCampusApp : Application() {

  override fun onCreate() {
    super.onCreate()
    instance = this

    // 1. Create Android O+ Notification Channels
    try {
      NotificationHelper.createNotificationChannels(this)
    } catch (e: Exception) {
      Log.w(TAG, "Notification channel creation error: ${e.message}")
    }

    // 2. Initialize local Room database
    try {
      OneCampusDatabase.getInstance(this)
    } catch (e: Exception) {
      Log.w(TAG, "Database init error: ${e.message}")
    }

    // 3. Initialize background sync manager
    try {
      SyncManager.getInstance(this).init()
    } catch (e: Exception) {
      Log.w(TAG, "SyncManager init error: ${e.message}")
    }

    // 4. Initialize Firebase & Register FCM token
    try {
      FirebaseApp.initializeApp(this)
      FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful && task.result != null) {
          val token = task.result
          Log.d(TAG, "Fetched FCM registration token: $token")
          CoroutineScope(Dispatchers.IO).launch {
            DeviceRepository.getInstance(applicationContext).registerToken(token)
          }
        } else {
          Log.w(TAG, "Fetching FCM registration token failed", task.exception)
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Firebase initialization error (safe in offline/emulator mode): ${e.message}")
    }
  }

  companion object {
    private const val TAG = "OneCampusApp"
    lateinit var instance: OneCampusApp
      private set

    fun getAppContext(): Context = instance.applicationContext
  }
}
