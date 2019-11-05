package com.tslex.radio

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private val TAG = this::class.java.canonicalName

        const val DATABASE_NAME = "appdb.db"
        const val DATABASE_VERSION = 2

        //RadioStation
        const val RADIO_STATION_TABLE_NAME = "RADIO_STATION"
        const val RADIO_STATION_ID = "_id"
        const val RADIO_STATION_NAME = "name"
        const val RADIO_STATION_IMAGE = "image_byte_array"
        const val RADIO_STATION_META = "meta_url"
        const val RADIO_STATION_STREAM = "stream_url"

        //StationHistory
        const val STATION_HISTORY_TABLE_NAME = "RADIO_STATION"
        const val STATION_HISTORY_ID = "_id"
        const val STATION_HISTORY_STATION_ID = "station_id"
        const val STATION_HISTORY_SONG_NAME = "song_name"
        const val STATION_HISTORY_ARTIST_NAME = "artist_name"

        const val SQL_RADIO_STATION_CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS $RADIO_STATION_TABLE_NAME " +
                        "(" +
                        "$RADIO_STATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "$RADIO_STATION_NAME TEXT NOT NULL, " +
                        "$RADIO_STATION_IMAGE TEXT NOT NULL, " +
                        "$RADIO_STATION_META TEXT NOT NULL, " +
                        "$RADIO_STATION_STREAM TEXT NOT NULL" +
                        ")"

        const val SQL_STATION_HISTORY_CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS $STATION_HISTORY_TABLE_NAME " +
                        "(" +
                        "$STATION_HISTORY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "$STATION_HISTORY_STATION_ID INTEGER, " +
                        "$STATION_HISTORY_SONG_NAME TEXT NOT NULL, " +
                        "$STATION_HISTORY_ARTIST_NAME TEXT NOT NULL" +
                        ")"


        const val SQL_RADIO_STATION_DELETE_TABLE = "DROP TABLE IF EXISTS $RADIO_STATION_TABLE_NAME"
        const val SQL_STATION_HISTORY_DELETE_TABLE = "DROP TABLE IF EXISTS $STATION_HISTORY_TABLE_NAME"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG, "onCreate")
        db?.execSQL(SQL_RADIO_STATION_CREATE_TABLE)
        db?.execSQL(SQL_STATION_HISTORY_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onDestroy")
        db?.execSQL(SQL_RADIO_STATION_DELETE_TABLE)
        db?.execSQL(SQL_STATION_HISTORY_DELETE_TABLE)
        onCreate(db)
    }
}