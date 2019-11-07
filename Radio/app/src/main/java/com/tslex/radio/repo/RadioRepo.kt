package com.tslex.radio.repo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.tslex.radio.DbHelper
import com.tslex.radio.domain.RadioStation

class RadioRepo(val context: Context) {

    private var TAG = this::class.java.canonicalName
    private lateinit var dbHelper: DbHelper
    private lateinit var db: SQLiteDatabase

    fun open(): RadioRepo {
        Log.d(TAG, "opened")

        dbHelper = DbHelper(context)
        db = dbHelper.writableDatabase
        return this
    }

    fun close() {
        Log.d(TAG, "closed")

        dbHelper.close()
    }

    fun erase(){
        db.execSQL(DbHelper.SQL_RADIO_STATION_DELETE_TABLE)
        db.execSQL(DbHelper.SQL_RADIO_STATION_CREATE_TABLE)
    }

    fun add(station: RadioStation) {
        Log.d(TAG, "added new station")

        val contentValue = ContentValues()
        contentValue.put(DbHelper.RADIO_STATION_NAME, station.stationName)
        contentValue.put(DbHelper.RADIO_STATION_IMAGE, station.stationImage)
        contentValue.put(DbHelper.RADIO_STATION_META, station.stationMeta)
        contentValue.put(DbHelper.RADIO_STATION_STREAM, station.stationStream)
        db.insert(DbHelper.RADIO_STATION_TABLE_NAME, null, contentValue)
    }

    private fun fetch(): Cursor {
        Log.d(TAG, "fetching")

        val columns = arrayOf(
                DbHelper.RADIO_STATION_ID,
                DbHelper.RADIO_STATION_NAME,
                DbHelper.RADIO_STATION_IMAGE,
                DbHelper.RADIO_STATION_META,
                DbHelper.RADIO_STATION_STREAM
        )

        val cursor = db.query(
                DbHelper.RADIO_STATION_TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
        )

        return cursor
    }

    fun getAll(): ArrayList<RadioStation> {

        Log.d(TAG, "getAll")

        val radioStations = ArrayList<RadioStation>()

        val cursor = fetch()

        Log.d(TAG, "counting...")

        if (cursor.count > 0) {

            Log.d(TAG, "moving...")
            cursor.moveToFirst()

            do {
                Log.d(TAG, "add...")
                radioStations.add(
                        RadioStation(
                                cursor.getInt(cursor.getColumnIndex(DbHelper.RADIO_STATION_ID)),
                                cursor.getString(cursor.getColumnIndex(DbHelper.RADIO_STATION_NAME)),
                                cursor.getString(cursor.getColumnIndex(DbHelper.RADIO_STATION_IMAGE)),
                                cursor.getString(cursor.getColumnIndex(DbHelper.RADIO_STATION_META)),
                                cursor.getString(cursor.getColumnIndex(DbHelper.RADIO_STATION_STREAM))
                        )
                )
            } while (cursor.moveToNext())
        }



        return radioStations
    }
}