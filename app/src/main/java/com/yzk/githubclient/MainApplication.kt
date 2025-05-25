package com.yzk.githubclient

import android.app.Application
import com.yzk.githubclient.security.SecuritySharedPreference
import dagger.hilt.android.HiltAndroidApp

/**
 * @description main application
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SecuritySharedPreference.initSecuritySharedPreference(this)
    }
}