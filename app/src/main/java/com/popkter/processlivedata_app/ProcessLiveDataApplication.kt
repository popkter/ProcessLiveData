package com.popkter.processlivedata_app

import android.app.Application
import android.util.Log
import android.widget.Button
import com.popkter.processlivedata.ProcessProvider
import com.popkter.processlivedata.putConfig
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProcessLiveDataApplication : Application() {
    private var count = 0

    override fun onCreate() {
        super.onCreate()
        MainScope().launch {
            count = 0
            while (count < 10) {
                delay(1000)
                contentResolver.putConfig("test_key", (Math.round(Math.random() * 1000)).toString())
                count++
            }
        }
    }
}