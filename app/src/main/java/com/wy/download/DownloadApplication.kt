package com.wy.download

import android.app.Application
import com.wy.download.db.DbManager
import com.wy.download.util.FileLoggingTree
import com.wy.nativelib.NativeLib
import timber.log.Timber

class DownloadApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(FileLoggingTree(this))
        DbManager.initDatabase(this)
        NativeLib.getInstance()
    }

    companion object {
        private var instance: DownloadApplication? = null
        fun getInstance(): DownloadApplication {
            if (instance == null) {
                instance = DownloadApplication()
            }
            return instance!!
        }
    }
}