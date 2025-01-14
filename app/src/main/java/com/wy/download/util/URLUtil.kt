package com.wy.download.util

import android.content.Context
import android.net.Uri
import com.dampcake.bencode.Bencode
import com.dampcake.bencode.BencodeException
import com.dampcake.bencode.Type
import com.wy.download.model.DownloadTaskInfo
import com.wy.download.model.TaskState
import com.wy.download.model.TaskTag
import com.wy.download.model.UrlCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object URLUtil {

    private val client by lazy {
        OkHttpClient.Builder()
            .followRedirects(true) // 跟随重定向
            .build()
    }

    //检测URL是否为合法的下载链接，包含Torrent文件检测
    suspend fun isValidDownloadLink(urlString: String): UrlCheckResult {
        return withContext(Dispatchers.IO) {
            // 验证 URL 格式
            val url: URL = try {
                URL(urlString)
            } catch (e: MalformedURLException) {
                return@withContext UrlCheckResult(false, "URL 格式不正确。")
            }

            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        // 可以进一步检查 Content-Type 是否为可下载的类型
                        val contentType = response.header("Content-Type") ?: "未知类型"
                        val contentLength = response.header("Content-Length")?.toLongOrNull() ?: -1

                        val category = contentType.toContentCategory()

                        if (category == ContentCategory.WEBPAGE) {
                            return@withContext UrlCheckResult(false, "这是一个网页")
                        } else if (contentLength <= 0) {
                            return@withContext UrlCheckResult(
                                false,
                                "无法获取文件大小，链接可能无效。"
                            )
                        } else if (category == ContentCategory.TORRENT) {
                            response.body?.bytes()?.let {
                                return@withContext isValidTorrent(it, urlString)
                            }
                        }

                        //检查响应头中是否包含文件名
                        val contentDisposition = response.header("Content-Disposition")
                        val fileName =
                            if (contentDisposition != null && contentDisposition.contains("filename=")) {
                                contentDisposition.split("filename=".toRegex())
                                    .dropLastWhile { it.isEmpty() }.toTypedArray()[1].replace(
                                    "\"",
                                    ""
                                )
                            } else {
                                urlString.substring(urlString.lastIndexOf('/') + 1)
                            }


                        // 检查响应头中是否包含哈希值
                        val hash = response.header("Content-MD5") ?: ""

                        val taskInfo = DownloadTaskInfo().apply {
                            taskId = StringUtil.generateTaskId()
                            this.url = urlString
                            tag = TaskTag.DIRECT.name
                            state = TaskState.WAITING
                            totalSize = contentLength
                            completedSize = 0L
                            this.hash = hash
                            this.fileName = fileName
                            savePath = MediaStoreUtils.getDownloadFilePath(fileName)
                        }

                        return@withContext UrlCheckResult(
                            true,
                            "链接有效。Content-Type: $contentType, Content-Length: $contentLength bytes.",
                            taskInfo
                        )
                    } else {
                        return@withContext UrlCheckResult(
                            false,
                            "服务器响应不成功，状态码：${response.code}."
                        )
                    }
                }
            } catch (e: Exception) {
                // 处理网络异常
                return@withContext UrlCheckResult(false, "网络异常：${e.localizedMessage}")
            }
        }
    }

    /**
     * 判断一个 URL 是否指向一个有效的种子文件。
     * @param url 要检测的 URL。
     * @return 如果是有效的种子文件，返回 true；否则，返回 false。
     */
    suspend fun isUrlTorrent(url: String): UrlCheckResult {
        // 1. 检查文件扩展名
        if (!url.lowercase().endsWith(".torrent")) {
            Timber.i("扩展名检测，不是有效的种子文件")
            return UrlCheckResult(false, "扩展名检测，不是有效的种子文件")
        }

        try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    // 可以进一步检查 Content-Type 是否为可下载的类型
                    val contentType = response.header("Content-Type")
                    if (contentType != null && !contentType.contains("application/x-bittorrent")) {
                        Timber.i("初步判断不是种子文件，contentType = $contentType")
                        return UrlCheckResult(false, "根据contentType判断该文件不是种子文件")
                    } else {
                        Timber.i("初步判断为种子文件，contentType = $contentType")
                    }

                    //下载并验证内容
                    response.body?.bytes()?.let {
                        return isValidTorrent(it, url)
                    }
                } else {
                    return UrlCheckResult(false, "请求URL地址失败")
                }
            }

        } catch (e: Exception) {
            Timber.i("error，message = ${e.message}")
            return UrlCheckResult(false, e.message ?: "URL请求异常")
        }
        return UrlCheckResult(false, "isUrlTorrent执行结束")
    }

    //检测URL是否为合法的种子文件, first: result, second: message
    suspend fun isLocalFileTorrent(context: Context, localFile: Uri): UrlCheckResult {
        val torrentBytes = withContext(Dispatchers.IO) {
            readFileBytes(context, localFile)
        } ?: return UrlCheckResult(false, "未读取到文件内容")
        return isValidTorrent(torrentBytes, localFile.toString())
    }

    /**
     * 从 URI 中读取文件内容为 ByteArray
     */
    private fun readFileBytes(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                readBytesFromInputStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 读取 InputStream 中的所有字节
     */
    private fun readBytesFromInputStream(inputStream: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(1024)
        var nRead: Int
        while (inputStream.read(data, 0, data.size).also { nRead = it } != -1) {
            buffer.write(data, 0, nRead)
        }
        buffer.flush()
        return buffer.toByteArray()
    }

    private suspend fun isValidTorrent(bytes: ByteArray, urlString: String): UrlCheckResult {
        return try {
            val bencodeUTF8 = Bencode(StandardCharsets.UTF_8)
            val dict: Map<String, Any> = bencodeUTF8.decode(bytes, Type.DICTIONARY)

            if (dict == null) {
                UrlCheckResult(false, "Bencode 解码失败")
            }
            if (!dict.containsKey("info")) {
                UrlCheckResult(false, "Info 属性丢失")
            }
            val info = dict["info"] as? Map<*, *> ?: return UrlCheckResult(false, "字段info不存在")
            if (!info.containsKey("piece length") || info["piece length"] !is Number) {
                UrlCheckResult(false, "字段 piece length 类型不合法")
            }
            if (!info.containsKey("name") || info["name"] !is String) {
                UrlCheckResult(false, "字段 name 类型不合法")
            }
            if (!info.containsKey("pieces") || info["pieces"] !is String) {
                UrlCheckResult(false, "字段 pieces 类型不合法")
            }
//            val taskInfo = DownloadTaskInfo().apply {
//                taskId = StringUtil.generateTaskId()
//                this.url = urlString
//                tag = TaskTag.TORRENT.name
//                state = TaskState.WAITING
//                totalSize = dict[""]
//                completedSize = 0L
//                this.hash = hash
//                this.fileName = info["name"].toString()
            //                savePath = MediaStoreUtils.getDownloadFilePath(fileName)

//            }
            // 所有检查通过
            UrlCheckResult(true, "种子合法", taskInfo)
        } catch (e: NullPointerException) {
            UrlCheckResult(false, "${e.message}")
        } catch (e: IllegalArgumentException) {
            UrlCheckResult(false, "${e.message}")
        } catch (e: BencodeException) {
            UrlCheckResult(false, "${e.message}")
        } catch (e: Exception) {
            println("发生错误: ${e.message}")
            UrlCheckResult(false, "${e.message}")
        }
    }

    fun isMagnetLink(url: String): UrlCheckResult {
        val magnetRegex = Regex(
            "^magnet:\\?xt=urn:btih:[A-Fa-f0-9]{40}" + // xt 参数
                    "(&dn=[^&]+)?" +                   // 允许可选的 dn 参数
                    "(&tr=https?://[^&]+)?" +          // 允许可选的 tr 参数
                    ".*$",
            RegexOption.IGNORE_CASE
        )
        val isMatch = magnetRegex.matches(url)
        if (isMatch) {
            val parseResult = parseMagnetLink(url)
            val taskInfo = DownloadTaskInfo().apply {
                taskId = StringUtil.generateTaskId()
                this.url = url
                tag = TaskTag.MAGNET.name
                state = TaskState.WAITING
                totalSize = parseResult["xl"]?.get(0)?.toLong() ?: 0L
                completedSize = 0L
                this.hash = ""
                this.fileName = parseResult["dn"]?.get(0) ?: ""
                savePath = MediaStoreUtils.getDownloadFilePath(fileName)
            }
            return UrlCheckResult(true, "合法的磁力链接", taskInfo)
        } else {
            return UrlCheckResult(false, "不是合法的磁力链接")
        }
    }

    private fun parseMagnetLink(magnetLink: String): Map<String, List<String>> {
        val params = mutableMapOf<String, MutableList<String>>()

        if (magnetLink.startsWith("magnet:?")) {
            val query = magnetLink.substring(8)
            val pairs = query.split("&")
            for (pair in pairs) {
                val idx = pair.indexOf('=')
                if (idx > 0 && idx < pair.length - 1) {
                    val key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
                    val value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                    params.computeIfAbsent(key) { mutableListOf() }.add(value)
                }
            }
        }

        return params
    }
}

// 定义内容类别的枚举类
enum class ContentCategory {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    ARCHIVE,
    WEBPAGE,
    TORRENT,
    UNKNOWN
}

// 扩展函数，将 Content-Type 映射到 ContentCategory
fun String.toContentCategory(): ContentCategory {
    return when {
        this.startsWith("image/") -> ContentCategory.IMAGE
        this.startsWith("video/") -> ContentCategory.VIDEO
        this.startsWith("audio/") -> ContentCategory.AUDIO
        this in listOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        ) -> ContentCategory.DOCUMENT

        this.startsWith("application/zip") ||
                this.startsWith("application/x-rar-compressed") ||
                this.startsWith("application/x-7z-compressed") -> ContentCategory.ARCHIVE

        this.startsWith("text/html") -> ContentCategory.WEBPAGE
        this.contains("application/x-bittorrent") -> ContentCategory.TORRENT
        else -> ContentCategory.UNKNOWN
    }
}