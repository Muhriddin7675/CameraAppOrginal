package com.example.cameraapporginal.app

import android.app.Application
import com.example.cameraapporginal.data.SharedPref

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SharedPref.init(this)
    }
}