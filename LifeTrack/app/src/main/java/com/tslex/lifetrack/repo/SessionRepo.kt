package com.tslex.lifetrack.repo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.tslex.lifetrack.DbHelper
import com.tslex.lifetrack.domain.Session
import java.sql.Time
import java.sql.Timestamp

class SessionRepo (val context: Context) {
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
                            Timestamp.valueOf(cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_CREATING_TIME))),
                            cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_WAYPOINT_SET)) == 1,
                            
                            cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_TOTAL_TIME)),
                            cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_PACE_START)),
                            cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_PACE_CP)),
                            cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_PACE_WP)),
                            cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_DDIST_START)),
                            cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_DDIST_CP)),
                            cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_DDIST_WP)),
                            cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_CDIST_START)),
                            cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_CDIST_CP)),
                            cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_CDIST_WP))
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
                    Timestamp.valueOf(cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_CREATING_TIME))),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_WAYPOINT_SET)) == 1,

                    cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_TOTAL_TIME)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_PACE_START)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_PACE_CP)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_PACE_WP)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_DDIST_START)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_DDIST_CP)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_DDIST_WP)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_CDIST_START)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_CDIST_CP)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_CDIST_WP))
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

        contentValue.put(DbHelper.SESSION_TOTAL_TIME, session.sessionTime)
        contentValue.put(DbHelper.SESSION_PACE_START, session.paceStart)
        contentValue.put(DbHelper.SESSION_PACE_CP, session.paceCp)
        contentValue.put(DbHelper.SESSION_PACE_WP, session.paceWp)
        contentValue.put(DbHelper.SESSION_DDIST_START, session.dirDistStart)
        contentValue.put(DbHelper.SESSION_DDIST_CP, session.dirDirCp)
        contentValue.put(DbHelper.SESSION_DDIST_WP, session.dirDirWp)
        contentValue.put(DbHelper.SESSION_CDIST_START, session.calDirStart)
        contentValue.put(DbHelper.SESSION_CDIST_CP, session.calDirCp)
        contentValue.put(DbHelper.SESSION_CDIST_WP, session.calDirWp)
        
        
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
                    Timestamp.valueOf(cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_CREATING_TIME))),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_WAYPOINT_SET)) == 1,

                    cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_TOTAL_TIME)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_PACE_START)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_PACE_CP)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_PACE_WP)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_DDIST_START)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_DDIST_CP)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_DDIST_WP)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_CDIST_START)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_CDIST_CP)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_CDIST_WP))
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

        contentValue.put(DbHelper.SESSION_TOTAL_TIME, session.sessionTime)
        contentValue.put(DbHelper.SESSION_PACE_START, session.paceStart)
        contentValue.put(DbHelper.SESSION_PACE_CP, session.paceCp)
        contentValue.put(DbHelper.SESSION_PACE_WP, session.paceWp)
        contentValue.put(DbHelper.SESSION_DDIST_START, session.dirDistStart)
        contentValue.put(DbHelper.SESSION_DDIST_CP, session.dirDirCp)
        contentValue.put(DbHelper.SESSION_DDIST_WP, session.dirDirWp)
        contentValue.put(DbHelper.SESSION_CDIST_START, session.calDirStart)
        contentValue.put(DbHelper.SESSION_CDIST_CP, session.calDirCp)
        contentValue.put(DbHelper.SESSION_CDIST_WP, session.calDirWp)


        db.update(DbHelper.SESSION_TABLE_NAME, contentValue, "" +
                "${DbHelper.SESSION_ID} = ${session.id}", null)
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun delete(id: Int){
        db.beginTransaction()
        db.execSQL("DELETE FROM ${DbHelper.SESSION_TABLE_NAME} WHERE ${DbHelper.SESSION_ID} = $id")
        db.setTransactionSuccessful()
        db.endTransaction()
    }
}

private fun Boolean.toInt(): Int? {
    return if (this) 1 else 0
}
