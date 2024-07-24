package com.popkter.processlivedata_app

import android.app.Application
import android.util.Log

class ProcessLiveDataApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.e("ProcessLiveDataApplication", "onCreate: ", )
    }
}