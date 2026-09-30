package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.AnnouncementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
  @Query("SELECT * FROM announcements ORDER BY createdAtEpochMs DESC")
  fun getAll(): Flow<List<AnnouncementEntity>>

  @Query("SELECT * FROM announcements WHERE id = :id LIMIT 1")
  fun getById(id: String): Flow<AnnouncementEntity?>

  @Query("SELECT * FROM announcements WHERE isBookmarked = 1 ORDER BY createdAtEpochMs DESC")
  fun getBookmarked(): Flow<List<AnnouncementEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(announcements: List<AnnouncementEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(announcement: AnnouncementEntity)

  @Query("UPDATE announcements SET isBookmarked = :isBookmarked WHERE id = :id")
  suspend fun updateBookmark(id: String, isBookmarked: Boolean)

  @Query("DELETE FROM announcements WHERE cachedAtEpochMs < :thresholdEpochMs AND isBookmarked = 0")
  suspend fun deleteOldCache(thresholdEpochMs: Long)

  @Query("DELETE FROM announcements")
  suspend fun clearAll()
}
