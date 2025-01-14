package com.wy.download.model

data class UrlCheckResult (
    val isValid: Boolean,
    val message: String = "",
    val taskInfo: DownloadTaskInfo? = null
)