package com.wy.download.exception

class FileCreateException(code: String = CODE, message: String = MESSAGE) : Exception(message) {

    companion object {
        const val CODE = "file_create"
        const val MESSAGE = "file create failed"
    }
}