package com.ccsmec.provider

import android.content.ContentResolver
import android.database.ContentObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.popkter.processlivedata.observerConfig
import com.popkter.processlivedata.putConfig

/**
 * LiveData Power by ContentProvider for IPC
 */

class ProcessLiveData(
    private val resolver: ContentResolver,
    private val key: String
) {

    private val liveData = SystemUIProviderLiveData(resolver, key)

    fun observe(lifecycleOwner: LifecycleOwner, onChanged: (String) -> Unit) {
        liveData.observe(lifecycleOwner) {
            onChanged.invoke(it)
        }
    }

    fun observeForever(onChanged: (String) -> Unit) {
        liveData.observeForever {
            onChanged.invoke(it)
        }
    }

    fun removeObserver(onChanged: (String) -> Unit) {
        liveData.removeObserver(onChanged)
    }

    fun removeObservers(lifecycleOwner: LifecycleOwner) {
        liveData.removeObservers(lifecycleOwner)
    }

    fun postValue(value: String) {
        liveData.updateConfig(value)
    }

    inner class SystemUIProviderLiveData(
        private val resolver: ContentResolver,
        private val key: String
    ) : LiveData<String>() {
        private lateinit var observer: ContentObserver

        override fun onActive() {
            observer = resolver.observerConfig(key) {
                postValue(it)
            }
        }

        override fun onInactive() {
            resolver.unregisterContentObserver(observer)
        }

        fun updateConfig(value: String) {
            resolver.putConfig(key, value)
        }
    }
}
