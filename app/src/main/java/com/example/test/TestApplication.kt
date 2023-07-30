package com.example.test

import android.app.Application
import com.example.test.application.SharedManager

class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        SharedManager.init(this)
    }
}