package com.slowly.manmanlai

import android.app.Application
import com.slowly.manmanlai.worker.NotificationHelper

class ManManLaiApp : Application() {
    val database by lazy { AppDatabase.create(this) }
    val repository by lazy { ManManLaiRepository(database) }
    val settings by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
