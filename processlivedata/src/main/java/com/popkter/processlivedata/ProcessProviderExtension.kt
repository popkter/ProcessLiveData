package com.popkter.processlivedata

import android.content.ContentResolver
import android.content.ContentValues
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.popkter.processlivedata.ProcessProvider.Companion.getContentUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


fun ContentResolver.putConfig(key: String, value: String) {
    CoroutineScope(Dispatchers.IO).launch {
        val entity = ContentValues().apply {
            put("key", key)
            put("value", value)
        }
        this@putConfig.insert(getContentUri(), entity)
    }
}

fun ContentResolver.getConfig(key: String, defaultValue: String): String {
    var result = defaultValue
    val cursor = this.query(getContentUri(),null,key,null,null)

    cursor?.run {
        moveToFirst()
        val valueIndex = cursor.getColumnIndex("value")
        result = cursor.getString(valueIndex)
        Log.i("ProcessProvider", " result= $result")
    }
    cursor?.close()
    return result
}

fun ContentResolver.observerConfig(key: String, block: (String) -> Unit): ContentObserver {
    val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            block.invoke(getConfig(key, ""))
        }
    }
    this.registerContentObserver(
        getContentUri(),
        false,
        observer
    )
    return observer
}
