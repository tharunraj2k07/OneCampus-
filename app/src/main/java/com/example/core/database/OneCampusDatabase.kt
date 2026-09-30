package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.core.database.dao.AnnouncementDao
import com.example.core.database.dao.NotificationDao
import com.example.core.database.dao.PendingSyncDao
import com.example.core.database.dao.TaskDao
import com.example.core.database.entities.AnnouncementEntity
import com.example.core.database.entities.NotificationEntity
import com.example.core.database.entities.PendingSyncEntity
import com.example.core.database.entities.TaskEntity

@Database(
  entities = [
    AnnouncementEntity::class,
    TaskEntity::class,
    NotificationEntity::class,
    PendingSyncEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class OneCampusDatabase : RoomDatabase() {
  abstract fun announcementDao(): AnnouncementDao
  abstract fun taskDao(): TaskDao
  abstract fun notificationDao(): NotificationDao
  abstract fun pendingSyncDao(): PendingSyncDao

  companion object {
    @Volatile
    private var INSTANCE: OneCampusDatabase? = null

    fun getInstance(context: Context): OneCampusDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          OneCampusDatabase::class.java,
          "onecampus_local.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
