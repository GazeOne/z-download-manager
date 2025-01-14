package com.wy.download.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chunk_info")
data class ChunkEntity(
    @PrimaryKey @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "index") val index: Int = 0,
    @ColumnInfo(name = "chunk_start") val chunkStart: Long = 0L,
    @ColumnInfo(name = "chunk_offset") val chunkOffset: Long = 0L,
    @ColumnInfo(name = "chunk_end") val chunkEnd: Long = 0L,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false
)
