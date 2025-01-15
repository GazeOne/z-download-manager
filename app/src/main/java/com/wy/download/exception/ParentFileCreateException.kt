package com.wy.download.exception

class ParentFileCreateException(code: String = CODE, message: String = MESSAGE) :
    Exception(message) {

    companion object {
        const val CODE = "parent_file_create"
        const val MESSAGE = "parent file create failed"
    }
}