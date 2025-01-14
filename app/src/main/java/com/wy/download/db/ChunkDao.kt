package com.wy.download.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ChunkDao {

    @Query("SELECT * FROM chunk_info WHERE task_id = :taskId")
    suspend fun getChunkById(taskId: String): ChunkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg chunkEntity: ChunkEntity)

    @Update
    suspend fun updateChunks(vararg chunkEntity: ChunkEntity)

    @Update
    suspend fun updateChunk(chunkEntity: ChunkEntity)

    @Delete
    suspend fun delete(chunkEntity: ChunkEntity)
}