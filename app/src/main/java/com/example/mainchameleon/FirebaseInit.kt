package com.example.mainchameleon

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class FirebaseInit : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Log.d("FirebaseInit", "Firebase initialized successfully")
    }
}