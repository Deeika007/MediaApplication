package com.example.mediaapplication

import android.app.Application
import com.google.firebase.FirebaseApp


class MediaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}