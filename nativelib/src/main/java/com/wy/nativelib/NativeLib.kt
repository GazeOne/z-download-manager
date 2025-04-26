package com.wy.nativelib

class NativeLib private constructor() {

    /**
     * A native method that is implemented by the 'nativelib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun setCallBack(callBack: CallBack)

    external fun addCallBack(callBack: CallBack)

    external fun notifyCallBackAll()

    val person = Person()

    val nativePtr: Long = 0

    external fun createNativeObject()

    external fun getNativeObjectName(prt: Long): String

    external fun getNativeObjectName2(): String

    companion object {
        @Volatile
        private var nativeLib: NativeLib? = null

        // Used to load the 'nativelib' library on application startup.
        init {
            System.loadLibrary("nativelib")
        }

        fun getInstance(): NativeLib {
            return nativeLib ?: synchronized(this) {
                nativeLib ?: NativeLib().also { nativeLib = it }
            }
        }
    }
}