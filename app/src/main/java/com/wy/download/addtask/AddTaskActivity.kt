package com.wy.download.addtask

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wy.download.addtask.ui.theme.ZdownloadmanagerTheme
import com.wy.download.util.URLUtil
import com.wy.download.util.findActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale

class AddTaskActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZdownloadmanagerTheme {
                AddTaskScreen()
            }
        }
    }
}

@Composable
fun AddTaskScreen() {
    var url by remember { mutableStateOf("") }
    var isTipsVisible by remember { mutableStateOf(false) }
    var tipsText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context.findActivity()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                Timber.d("uri = $it")
                coroutineScope.launch(Dispatchers.IO) {
                    url = getFileName(context, it) ?: "为获取到名称"
                    val checkResult = URLUtil.isLocalFileTorrent(context, it)
                    if (checkResult.isValid) {
                        //TODO 合法的种子文件，创建任务
                        activity?.finish()
                    } else {
                        isTipsVisible = true
                        tipsText = checkResult.message ?: ""
                    }
                }
            }
        }

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            TopBar {
                coroutineScope.launch {
                    Timber.d("check url, url = $url")
                    if (url.lowercase(Locale.getDefault()).startsWith("magnet:")) {
                        val checkResult = URLUtil.isMagnetLink(url)
                        if (checkResult.isValid) {
                            //TODO 合法的磁力链接，创建任务
                            activity?.finish()
                        } else {
                            isTipsVisible = true
                            tipsText = "该磁力链接不合法"
                        }
                    } else if (url.endsWith(".torrent")) {
                        val isValidTorrent = URLUtil.isUrlTorrent(url)
                        if (isValidTorrent.first) {
                            //TODO 合法的种子文件，创建任务
                            activity?.finish()
                        } else {
                            isTipsVisible = true
                            tipsText = isValidTorrent.second
                        }
                    } else {
                        val checkResult = URLUtil.isValidDownloadLink(url)
                        if (checkResult.isValid) {
                            //TODO 合法的直接下载链接，创建任务
                            activity?.finish()
                        } else {
                            isTipsVisible = true
                            tipsText = checkResult.message
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                "输入URL或选择种子文件",
                maxLines = 1,
                modifier = Modifier.padding(16.dp),
                overflow = TextOverflow.Ellipsis
            )
            Row(modifier = Modifier.padding(10.dp)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                )
                IconButton(modifier = Modifier.padding(10.dp),
                    onClick = { launcher.launch(arrayOf("application/x-bittorrent")) }) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = "Close"
                    )
                }
            }
            if (isTipsVisible) {
                Text(tipsText, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(checkUrl: () -> Unit) {
    val context = LocalContext.current.findActivity()

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                "添加URL或者种子文件",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = { context?.finish() }) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close"
                )
            }
        },
        actions = {
            TextButton(onClick = {
                checkUrl.invoke()
            }) {
                Text("确定")
            }
        },
    )
}

private fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
    }
    if (name == null) {
        name = uri.path?.substringAfterLast('/')
    }
    return name
}