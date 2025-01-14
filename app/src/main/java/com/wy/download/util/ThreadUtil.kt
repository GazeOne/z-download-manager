package com.wy.download.util

import java.util.concurrent.Executors

class ThreadUtil {

    private val taskThreadPool = Executors.newFixedThreadPool(CONCURRENT_TASK_NUM)
    private val chunkThreadPool = Executors.newWorkStealingPool(CONCURRENT_TASK_CHUNK_NUM)

    fun runInTaskThread(runnable: Runnable) {
        taskThreadPool.submit(runnable)
    }

    fun runInChunkThread(runnable: Runnable) {
        chunkThreadPool.submit(runnable)
    }

    companion object {
        private const val CONCURRENT_TASK_NUM = 3
        private const val CONCURRENT_TASK_CHUNK_NUM = 9
    }

}