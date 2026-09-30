package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
  @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, CASE WHEN deadlineEpochMs IS NULL THEN 1 ELSE 0 END, deadlineEpochMs ASC")
  fun getAll(): Flow<List<TaskEntity>>

  @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY CASE WHEN deadlineEpochMs IS NULL THEN 1 ELSE 0 END, deadlineEpochMs ASC")
  fun getPendingTasks(): Flow<List<TaskEntity>>

  @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY updatedAtEpochMs DESC")
  fun getCompletedTasks(): Flow<List<TaskEntity>>

  @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
  suspend fun getById(id: String): TaskEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(tasks: List<TaskEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(task: TaskEntity)

  @Query("UPDATE tasks SET status = :status, isCompleted = :isCompleted, syncStatus = :syncStatus, updatedAtEpochMs = :updatedAt WHERE id = :id")
  suspend fun updateTaskStatus(
    id: String,
    status: String,
    isCompleted: Boolean,
    syncStatus: String = "PENDING_UPDATE",
    updatedAt: Long = System.currentTimeMillis()
  )

  @Query("SELECT * FROM tasks WHERE syncStatus = 'PENDING_UPDATE'")
  suspend fun getPendingSyncTasks(): List<TaskEntity>

  @Query("DELETE FROM tasks")
  suspend fun clearAll()
}
