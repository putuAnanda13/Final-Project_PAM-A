package com.example.healthcal

import android.app.Application

class HealthCalApp : Application() {
    companion object {
        lateinit var instance: HealthCalApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
