package com.tslex.lifetrack.repo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.tslex.lifetrack.DbHelper
import com.tslex.lifetrack.domain.Session
import java.sql.Time

class SessionRepo(val context: Context) {
    private var TAG = this::class.java.canonicalName
    private lateinit var dbHelper: DbHelper
    private lateinit var db: SQLiteDatabase

    fun open(): SessionRepo {
        Log.d(TAG, "opened")

        dbHelper = DbHelper(context)
        db = dbHelper.writableDatabase
        return this
    }

    fun close() {
        Log.d(TAG, "closed")

        dbHelper.close()
    }

    fun getAll(): ArrayList<Session> {
        val cursor = db.rawQuery("SELECT * FROM ${DbHelper.SESSION_TABLE_NAME}", null)
        val sessions = ArrayList<Session>()

        cursor.use {
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    sessions.add(
                        Session(
                            cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_ID)),
                            cursor.getDouble(cursor.getColumnIndex(DbHelper.SESSION_WAYPOINT_LAT)),
                            cursor.getDouble(cursor.getColumnIndex(DbHelper.SESSION_WAYPOINT_LNG)),
                            Time.valueOf(cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_CREATING_TIME))),
                            cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_WAYPOINT_SET)) == 1
                        )
                    )

                } while (cursor.moveToNext())
            }
        }

        return sessions
    }

    fun getById(id: Int): Session? {
        val cursor = db.rawQuery(
            "SELECT * FROM ${DbHelper.SESSION_TABLE_NAME} WHERE ${DbHelper.SESSION_ID} = $id",
            null
        )
        cursor.use { cursor ->
            if (cursor.count > 0) {
                cursor.moveToFirst()
                return Session(
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_ID)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.SESSION_WAYPOINT_LAT)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.SESSION_WAYPOINT_LNG)),
                    Time.valueOf(cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_CREATING_TIME))),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_WAYPOINT_SET)) == 1
                )
            }
            return null
        }
    }

    fun add(session: Session) {
        db.beginTransaction()
        val contentValue = ContentValues()
        contentValue.put(DbHelper.SESSION_WAYPOINT_LAT, session.wLat)
        contentValue.put(DbHelper.SESSION_WAYPOINT_LNG, session.wLng)
        contentValue.put(DbHelper.SESSION_CREATING_TIME, session.creatingTime.toString())
        contentValue.put(DbHelper.SESSION_WAYPOINT_SET, (session.isWayPointSet.toInt()))
        db.insert(DbHelper.SESSION_TABLE_NAME, null, contentValue)
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun getLast(): Session?{
        val cursor = db.rawQuery("select * from ${DbHelper.SESSION_TABLE_NAME} " +
                "order by ${DbHelper.SESSION_ID} DESC",
            null)

        cursor.use { cursor ->
            if (cursor.count > 0){
                cursor.moveToFirst()
                return Session(
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_ID)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.SESSION_WAYPOINT_LAT)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.SESSION_WAYPOINT_LNG)),
                    Time.valueOf(cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_CREATING_TIME))),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_WAYPOINT_SET)) == 1
                )
            }
            return null
        }
    }

    fun update(session: Session){
        db.beginTransaction()
        val contentValue = ContentValues()
        contentValue.put(DbHelper.SESSION_WAYPOINT_LAT, session.wLat)
        contentValue.put(DbHelper.SESSION_WAYPOINT_LNG, session.wLng)
        contentValue.put(DbHelper.SESSION_CREATING_TIME, session.creatingTime.toString())
        contentValue.put(DbHelper.SESSION_WAYPOINT_SET, (session.isWayPointSet.toInt()))

        db.update(DbHelper.SESSION_TABLE_NAME, contentValue, "" +
                "${DbHelper.SESSION_ID} = ${session.id}", null)
        db.setTransactionSuccessful()
        db.endTransaction()
    }
}

private fun Boolean.toInt(): Int? {
    return if (this) 1 else 0
}
