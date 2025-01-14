package com.wy.download

import android.app.Application
import com.wy.download.db.DbManager
import com.wy.download.util.FileLoggingTree
import timber.log.Timber

class DownloadApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(FileLoggingTree(this))
        DbManager.initDatabase(this)
    }
}