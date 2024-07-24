package com.popkter.processlivedata

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

open class ProcessProvider : ContentProvider() {


    companion object {
        var AUTHORITY = "com.popkter.processlivedata"
        var PATH_CONFIG = "config"

        @JvmStatic
        fun getContentUri(): Uri {
            return Uri.parse("content://$AUTHORITY/$PATH_CONFIG")
        }
    }


    private lateinit var db: IpcConfigDatabase

    override fun onCreate(): Boolean {
        db = Room.databaseBuilder(
            context!!,
            IpcConfigDatabase::class.java, "IpcConfig.db"
        ).build()
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?,
        selection: String?, selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        selection ?: return null
        val entity = db.configDao().getConfigByKey(selection)
        val cursor = entity?.let { systemUIConfigEntityToCursor(it) }
        cursor?.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri) = null

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        values ?: return uri
        db.configDao().insertConfig(
            SystemUIConfigEntity(
                key = values.getAsString("key"),
                value = values.getAsString("value")
            )
        )
        context?.contentResolver?.notifyChange(uri, null)
        return uri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        selection ?: return -1
        val config = db.configDao().getConfigByKey(selection)
        if (config != null) {
            db.configDao().deleteConfig(config)
            context?.contentResolver?.notifyChange(uri, null)
            return 1
        }
        return 0
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}

@Entity(tableName = "config")
data class SystemUIConfigEntity(
    @PrimaryKey
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "value") val value: String
)


fun systemUIConfigEntityToCursor(entity: SystemUIConfigEntity): Cursor {
    val cursor = MatrixCursor(arrayOf("key", "value"))
    cursor.addRow(arrayOf(entity.key, entity.value))
    return cursor
}

@Dao
interface SystemUIConfigDao {
    @Query("SELECT * FROM config")
    fun getAllConfigs(): List<SystemUIConfigEntity>

    @Query("SELECT * FROM config WHERE `key` = :key")
    fun getConfigByKey(key: String): SystemUIConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConfig(config: SystemUIConfigEntity)

    @Delete
    fun deleteConfig(config: SystemUIConfigEntity)
}

@Database(entities = [SystemUIConfigEntity::class], version = 1, exportSchema = false)
abstract class IpcConfigDatabase : RoomDatabase() {
    abstract fun configDao(): SystemUIConfigDao
}
