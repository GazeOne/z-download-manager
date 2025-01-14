package com.wy.download.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {

    @Query("SELECT * FROM task_info")
    suspend fun getAllTask(): List<TaskEntity>?

    @Query("SELECT * FROM task_info WHERE task_id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Query("SELECT * FROM task_info WHERE tag = :tag")
    suspend fun getTasksByTag(tag: String): List<TaskEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg taskEntity: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingle(taskEntity: TaskEntity)

    @Update
    suspend fun updateTasks(vararg taskEntity: TaskEntity)

    @Update
    suspend fun updateTask(taskEntity: TaskEntity)

    @Delete
    suspend fun delete(taskEntity: TaskEntity)
}