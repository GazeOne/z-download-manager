package com.wy.download.home

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wy.nativelib.CallBack
import com.wy.nativelib.NativeLib

@Composable
fun HomeScreen() {
    val nativeLib = NativeLib.getInstance()
    nativeLib.addCallBack(object : CallBack {
        override fun onSuccess(success: String) {
            Log.d("HomeScreen", "wy $success")
        }

        override fun onFailed(fail: String) {
            Log.d("HomeScreen", "wy $fail")
        }

    })
    Scaffold (modifier = Modifier.fillMaxWidth()) { innerPadding ->
        Column {
            Text(
                text = /*"Hello HomeScreen!"*/ nativeLib.stringFromJNI() + " : " + nativeLib.person,
                modifier = Modifier.padding(innerPadding)
            )
            Button(
                onClick = {
                    nativeLib.notifyCallBackAll()
                }) {
                Text(text = "最简单Button")
            }
        }
    }
}