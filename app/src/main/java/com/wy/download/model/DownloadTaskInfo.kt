package com.wy.download.model

class DownloadTaskInfo {
    var taskId: String = ""
    var url: String = ""
    var tag: String = ""
    var state: TaskState = TaskState.WAITING
    var totalSize = 0L
    var completedSize = 0L
    var hash: String = ""
    var savePath: String = ""
    var fileName: String = ""
    var torrentFile: List<TorrentFile>? = null
    var extras = mutableMapOf<String, String>()
}