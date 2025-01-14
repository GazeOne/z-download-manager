package com.wy.download.util

import java.util.UUID

object StringUtil {

    fun generateTaskId(): String {
        return "T-${UUID.randomUUID()}"
    }
}