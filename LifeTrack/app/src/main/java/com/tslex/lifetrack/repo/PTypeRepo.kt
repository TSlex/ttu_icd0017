package com.tslex.lifetrack.repo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.tslex.lifetrack.DbHelper
import com.tslex.lifetrack.domain.PType

class PTypeRepo(val context: Context) {
    private var TAG = this::class.java.canonicalName
    private lateinit var dbHelper: DbHelper
    private lateinit var db: SQLiteDatabase

    fun open(): PTypeRepo {
        Log.d(TAG, "opened")

        dbHelper = DbHelper(context)
        db = dbHelper.writableDatabase
        return this
    }

    fun close() {
        Log.d(TAG, "closed")

        dbHelper.close()
    }

    fun getAll(): ArrayList<PType> {
        val cursor = db.rawQuery("SELECT * FROM ${DbHelper.POINT_TYPE_TABLE_NAME}", null)
        var pTypes = ArrayList<PType>()

        cursor.use { cursor ->
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    pTypes.add(
                        PType(
                            cursor.getInt(cursor.getColumnIndex(DbHelper.POINT_TYPE_ID)),
                            cursor.getString(cursor.getColumnIndex(DbHelper.POINT_TYPE))
                        )
                    )
                } while (cursor.moveToNext())
            }
        }
        return pTypes
    }
}