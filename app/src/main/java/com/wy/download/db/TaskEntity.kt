package com.wy.download.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wy.download.model.TaskState

@Entity(tableName = "task_info")
data class TaskEntity(
    @PrimaryKey @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "url") val url: String = "",
    @ColumnInfo(name = "tag") val tag: String = "",
    @ColumnInfo(name = "type") val type: Int = -1,
    @ColumnInfo(name = "state") val state: TaskState = TaskState.WAITING,
    @ColumnInfo(name = "total_size") val totalSize: Long = 0L,
    @ColumnInfo(name = "completed_size") val completedSize: Long = 0L,
    @ColumnInfo(name = "hash") val hash: String = "",
    @ColumnInfo(name = "save_path") val savePath: String = "",
    @ColumnInfo(name = "file_name") val fileName: String = "",
    @ColumnInfo(name = "extras") val extras: String = ""
)
