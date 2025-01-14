package com.wy.download.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.channels.Channel
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import timber.log.Timber

/**
 * OptimizedFileLoggingTree 使用 Kotlin 协程和 Channel 实现高效的日志批量写入和缓冲机制。
 */
class FileLoggingTree(
    context: Context,
    private val logDirectoryName: String = "logs",
    private val logFilePrefix: String = "app_logs_",
    private val logFileSuffix: String = ".txt",
    private val batchSize: Int = 50,
    private val flushIntervalMs: Long = 5000L,
    private val retainDays: Int = 7
) : Timber.Tree() {

    private val logDirectory: File = File(context.filesDir, logDirectoryName).apply {
        if (!exists()) mkdirs()
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val logTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    private val logChannel = Channel<String>(capacity = Channel.UNLIMITED)
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentDate: String = dateFormat.format(Date())
    private var bufferedWriter: BufferedWriter? = initializeBufferedWriter(currentDate)

    init {
        // 启动日志写入协程
        coroutineScope.launch {
            processLogs()
        }

        // 清理旧日志
        coroutineScope.launch {
            cleanOldLogs()
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.DEBUG) return

        val timestamp = logTimeFormat.format(Date())
        val priorityStr = priorityToString(priority)
        val logTag = tag ?: "NoTag"
        val logMessage = "$timestamp $priorityStr/$logTag: $message\n"

        // 将日志消息发送到 Channel
        logChannel.trySend(logMessage)
    }

    /**
     * 处理日志的协程函数，负责批量写入和时间间隔写入。
     */
    private suspend fun processLogs() {
        val batch = mutableListOf<String>()
        while (isActive) {
            try {
                val log = withTimeoutOrNull(flushIntervalMs) {
                    logChannel.receive()
                }

                if (log != null) {
                    batch.add(log)
                    if (batch.size >= batchSize) {
                        writeBatchLogs(batch)
                        batch.clear()
                    }
                }

                // 如果达到时间间隔，批量写入
                if (batch.isNotEmpty() && (log == null || batch.size >= batchSize)) {
                    writeBatchLogs(batch)
                    batch.clear()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing logs", e)
            }
        }

        // 处理剩余的日志
        if (batch.isNotEmpty()) {
            writeBatchLogs(batch)
            batch.clear()
        }

        // 关闭 BufferedWriter
        bufferedWriter?.flush()
        bufferedWriter?.close()
    }

    /**
     * 将日志批量写入文件
     */
    private fun writeBatchLogs(batch: List<String>) {
        val today = dateFormat.format(Date())
        if (today != currentDate) {
            // 日志日期变化，切换日志文件
            bufferedWriter?.flush()
            bufferedWriter?.close()
            bufferedWriter = initializeBufferedWriter(today)
            currentDate = today
        }

        try {
            bufferedWriter?.let { writer ->
                for (log in batch) {
                    writer.write(log)
                }
                writer.flush()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error writing batch logs to file", e)
        }
    }

    /**
     * 初始化 BufferedWriter
     */
    private fun initializeBufferedWriter(date: String): BufferedWriter? {
        return try {
            val logFile = getLogFile(date)
            BufferedWriter(FileWriter(logFile, true))
        } catch (e: IOException) {
            Log.e(TAG, "Error initializing BufferedWriter", e)
            null
        }
    }

    /**
     * 根据日期获取日志文件
     */
    private fun getLogFile(date: String): File {
        return File(logDirectory, "$logFilePrefix$date$logFileSuffix")
    }

    /**
     * 将日志优先级转为字符串
     */
    private fun priorityToString(priority: Int): String {
        return when (priority) {
            Log.VERBOSE -> "VERBOSE"
            Log.DEBUG -> "DEBUG"
            Log.INFO -> "INFO"
            Log.WARN -> "WARN"
            Log.ERROR -> "ERROR"
            Log.ASSERT -> "ASSERT"
            else -> "UNKNOWN"
        }
    }

    /**
     * 清理过期的日志文件，保留最近 retainDays 天的日志
     */
    private suspend fun cleanOldLogs() {
        withContext(Dispatchers.IO) {
            val threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retainDays.toLong())
            logDirectory.listFiles()?.forEach { file ->
                if (file.isFile) {
                    if (file.lastModified() < threshold) {
                        val deleted = file.delete()
                        if (!deleted) {
                            Log.w(TAG, "Unable to delete old log file: ${file.name}")
                        }
                    }
                }
            }
        }
    }

    /**
     * 关闭日志写入协程和 Channel
     */
    fun shutdown() {
        coroutineScope.cancel()
        logChannel.close()
    }

    companion object {
        private const val TAG = "OptimizedFileLoggingTree"
    }
}

