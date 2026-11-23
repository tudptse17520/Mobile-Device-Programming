package com.example.uthsmarttasks

import android.app.Application
import com.example.uthsmarttasks.notifications.UthMessagingService

class UthApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UthMessagingService.createChannel(this)
    }
}
