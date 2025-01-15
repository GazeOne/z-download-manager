package com.wy.download.util

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import androidx.annotation.RequiresApi
import com.wy.download.DownloadApplication
import com.wy.download.exception.EmptyPathException
import com.wy.download.exception.FileCreateException
import com.wy.download.exception.ParentFileCreateException
import com.wy.download.exception.ParentFileNullException
import java.io.File
import java.io.IOException
import java.net.URLEncoder

object FileUtil {
    private val ROOT_PATH =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "z-download_manager"
    private const val ILLEGAL_PATTERN = "[/*\\\\?:<>\"|\\t\\r\\n]"

    private val context by lazy { DownloadApplication.getInstance() }

    // 文件绝对路径
    fun getDownloadFilePath(tag: String, fileName: String): String {
        val name = if (isLegal(fileName)) {
            fileName
        } else {
            encodeFileName(fileName)
        }
        return ROOT_PATH + File.separator + tag + File.separator + fileName
    }


    // Android9 以下使用绝对路径创建文件
    fun getOrCreateFile(path: String): File {
        if (TextUtils.isEmpty(path)) {
            throw EmptyPathException()
        }

        val file = File(path)
        val parentFile = file.parentFile ?: throw ParentFileNullException()

        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw ParentFileCreateException()
        }

        try {
            val isSuccess = file.createNewFile()
            if (!isSuccess) {
                throw FileCreateException()
            }
        } catch (e: IOException) {
            throw FileCreateException(e.message ?: "")
        }
        return file
    }

    // 创建下载文件（Android 10及以上）
    @RequiresApi(Build.VERSION_CODES.Q)
    fun createDownloadFile(tag: String, fileName: String, mimeType: String?): Uri? {
        val name = if (isLegal(fileName)) {
            fileName
        } else {
            encodeFileName(fileName)
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, tag + name)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, "${ROOT_PATH}${File.separator}$tag")
        }

        return context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    private fun isLegal(string: String): Boolean {
        if (TextUtils.isEmpty(string)) {
            return false
        }
        return !string.contains(Regex(ILLEGAL_PATTERN))
    }

    private fun encodeFileName(name: String): String {
        return name.map { char ->
            if (Regex(ILLEGAL_PATTERN).matches(char.toString())) {
                URLEncoder.encode(char.toString(), "UTF-8") // 对非法字符进行 URL 编码
            } else {
                char.toString()
            }
        }.joinToString("")
    }
}
