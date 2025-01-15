package com.wy.download.exception

class EmptyPathException(code: String = CODE, message: String = MESSAGE) : Exception(message) {

    companion object {
        const val CODE = "empty_path"
        const val MESSAGE = "path is empty"
    }
}