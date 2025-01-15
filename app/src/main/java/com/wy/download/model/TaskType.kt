package com.wy.download.model

enum class TaskType {
    UNKNOWN,
    DIRECT,
    TORRENT,
    MAGNET
}

enum class TaskTag {
    DIRECT,
    SINGLE_FILE_TORRENT,
    MULTI_FILE_TORRENT,
    MAGNET,
}