package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.PendingSyncEntity

@Dao
interface PendingSyncDao {
  @Query("SELECT * FROM pending_sync_queue ORDER BY timestampEpochMs ASC")
  suspend fun getAll(): List<PendingSyncEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(item: PendingSyncEntity)

  @Query("DELETE FROM pending_sync_queue WHERE operationId = :operationId")
  suspend fun deleteById(operationId: String)

  @Query("UPDATE pending_sync_queue SET retryCount = retryCount + 1 WHERE operationId = :operationId")
  suspend fun incrementRetry(operationId: String)

  @Query("DELETE FROM pending_sync_queue")
  suspend fun clearAll()
}
