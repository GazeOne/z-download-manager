package com.wy.download.model

enum class TaskType(value: Int) {
    UNKNOWN(-1),
    DIRECT(0),
    TORRENT(1),
    MAGNET(2)
}

enum class TaskTag(value: String) {
    DIRECT("DIRECT"),
    TORRENT("TORRENT"),
    MAGNET("MAGNET"),
}