package com.example.firebaseauth

import android.app.Application
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class UthApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3600).build())
            setDefaultsAsync(R.xml.remote_config_defaults)
        }
    }
}
