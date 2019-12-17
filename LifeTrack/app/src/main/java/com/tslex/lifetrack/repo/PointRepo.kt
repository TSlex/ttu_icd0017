package com.tslex.lifetrack.repo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.tslex.lifetrack.DbHelper
import com.tslex.lifetrack.domain.Point
import java.sql.Time

class PointRepo(val context: Context) {
    private var TAG = this::class.java.canonicalName
    private lateinit var dbHelper: DbHelper
    private lateinit var db: SQLiteDatabase

    fun open(): PointRepo {
        Log.d(TAG, "opened")

        dbHelper = DbHelper(context)
        db = dbHelper.writableDatabase
        return this
    }

    fun close() {
        Log.d(TAG, "closed")

        dbHelper.close()
    }

    fun getAll(sessionId: Int): ArrayList<Point> {

        Log.d(TAG, "getAll")

        val radioStations = ArrayList<Point>()

        val cursor = db.rawQuery("SELECT * FROM ${DbHelper.POINT_TABLE_NAME}" +
                "where ${DbHelper.POINT_SESSION_ID} == $sessionId ", null)

        Log.d(TAG, "counting...")

        cursor.use {
            if (cursor.count > 0) {

                Log.d(TAG, "moving...")
                cursor.moveToFirst()

                do {
                    Log.d(TAG, "add...")
                    radioStations.add(
                        Point(
                            cursor.getInt(cursor.getColumnIndex(DbHelper.POINT_ID)),
                            cursor.getInt(cursor.getColumnIndex(DbHelper.POINT_SESSION_ID)),
                            cursor.getInt(cursor.getColumnIndex(DbHelper.POINT_PTYPE_ID)),
                            cursor.getDouble(cursor.getColumnIndex(DbHelper.POINT_LAT)),
                            cursor.getDouble(cursor.getColumnIndex(DbHelper.POINT_LNG)),
                            Time.valueOf(cursor.getString(cursor.getColumnIndex(DbHelper.POINT_CREATING_TIME)))
                        )
                    )
                } while (cursor.moveToNext())
            }
        }

        return radioStations
    }

    fun getLast(sessionId: Int): Point?{
        val cursor = db.rawQuery("select * from ${DbHelper.SESSION_TABLE_NAME} " +
                "where ${DbHelper.POINT_SESSION_ID} == $sessionId " +
                "order by ${DbHelper.SESSION_ID} DESC",
            null)

        cursor.use { cursor ->
            if (cursor.count > 0){
                cursor.moveToFirst()
                return Point(
                    cursor.getInt(cursor.getColumnIndex(DbHelper.POINT_ID)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.POINT_SESSION_ID)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.POINT_PTYPE_ID)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.POINT_LAT)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.POINT_LNG)),
                    Time.valueOf(cursor.getString(cursor.getColumnIndex(DbHelper.POINT_CREATING_TIME)))
                )
            }
            return null
        }
    }

    fun getFirst(sessionId: Int): Point?{
        val cursor = db.rawQuery("select * from ${DbHelper.SESSION_TABLE_NAME} " +
                "where ${DbHelper.POINT_SESSION_ID} == $sessionId " +
                "order by ${DbHelper.SESSION_ID}",
            null)

        cursor.use { cursor ->
            if (cursor.count > 0){
                cursor.moveToFirst()
                return Point(
                    cursor.getInt(cursor.getColumnIndex(DbHelper.POINT_ID)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.POINT_SESSION_ID)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.POINT_PTYPE_ID)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.POINT_LAT)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.POINT_LNG)),
                    Time.valueOf(cursor.getString(cursor.getColumnIndex(DbHelper.POINT_CREATING_TIME)))
                )
            }
            return null
        }
    }

    fun add(point: Point) {
        val contentValue = ContentValues()
        contentValue.put(DbHelper.POINT_SESSION_ID, point.sessionId)
        contentValue.put(DbHelper.POINT_PTYPE_ID, point.typeId)
        contentValue.put(DbHelper.POINT_LAT, point.pLat)
        contentValue.put(DbHelper.POINT_LNG, point.pLng)
        contentValue.put(DbHelper.POINT_CREATING_TIME, point.timeOfCreating.toString())
        db.insert(DbHelper.POINT_TABLE_NAME, null, contentValue)
    }
}