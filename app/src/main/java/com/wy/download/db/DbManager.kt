package com.wy.download.db

import android.content.Context
import androidx.room.Room

object DbManager {
    private lateinit var taskDao: TaskDao
    private lateinit var chunkDao: ChunkDao
    private lateinit var db: AppDatabase

    fun initDatabase(context: Context) {
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "database-name"
        ).build()

        taskDao = db.taskDao()
        chunkDao = db.chunkDao()
    }

    suspend fun getTasks(): List<TaskEntity>? {
        return taskDao.getAllTask()
    }
}