package com.tslex.radio.repo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.tslex.radio.DbHelper
import com.tslex.radio.domain.RadioStation
import com.tslex.radio.domain.StationHistory
import java.sql.Time

class HistoryRepo(val context: Context) {

    private var TAG = this::class.java.canonicalName
    private lateinit var dbHelper: DbHelper
    private lateinit var db: SQLiteDatabase

    fun open(): HistoryRepo {
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
        db.execSQL(DbHelper.SQL_STATION_HISTORY_DELETE_TABLE)
        db.execSQL(DbHelper.SQL_STATION_HISTORY_CREATE_TABLE)
    }

    fun add(history: StationHistory) {
        Log.d(TAG, "added new station")

        val contentValue = ContentValues()
        contentValue.put(DbHelper.STATION_HISTORY_STATION_ID, history.stationId)
        contentValue.put(DbHelper.STATION_HISTORY_SONG_NAME, history.songName)
        contentValue.put(DbHelper.STATION_HISTORY_ARTIST_NAME, history.artistName)
        contentValue.put(DbHelper.STATION_HISTORY_PlAYED_COUNT, history.playedCount)
        contentValue.put(DbHelper.STATION_HISTORY_LAST_PLAYED, history.lastPlayedTime.toString())
        db.insert(DbHelper.STATION_HISTORY_TABLE_NAME, null, contentValue)
    }

    private fun fetch(): Cursor {
        Log.d(TAG, "fetching")

        val columns = arrayOf(
                DbHelper.STATION_HISTORY_ID,
                DbHelper.STATION_HISTORY_STATION_ID,
                DbHelper.STATION_HISTORY_SONG_NAME,
                DbHelper.STATION_HISTORY_ARTIST_NAME,
                DbHelper.STATION_HISTORY_PlAYED_COUNT,
                DbHelper.STATION_HISTORY_LAST_PLAYED
        )

        val cursor = db.query(
                DbHelper.STATION_HISTORY_TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
        )

        return cursor
    }

    fun getAll(): ArrayList<StationHistory> {

        Log.d(TAG, "getAll")

        val stationHistories = ArrayList<StationHistory>()

        val cursor = fetch()

        Log.d(TAG, "counting...")

        if (cursor.count > 0) {

            Log.d(TAG, "moving...")
            cursor.moveToFirst()

            do {
                Log.d(TAG, "add...")
                stationHistories.add(
                        StationHistory(
                                cursor.getInt(cursor.getColumnIndex(DbHelper.STATION_HISTORY_ID)),
                                cursor.getString(cursor.getColumnIndex(DbHelper.STATION_HISTORY_SONG_NAME)),
                                cursor.getString(cursor.getColumnIndex(DbHelper.STATION_HISTORY_ARTIST_NAME)),
                                cursor.getInt(cursor.getColumnIndex(DbHelper.STATION_HISTORY_STATION_ID)),
                                cursor.getInt(cursor.getColumnIndex(DbHelper.STATION_HISTORY_PlAYED_COUNT)),
                                Time.valueOf(cursor.getString(cursor.getColumnIndex(DbHelper.STATION_HISTORY_LAST_PLAYED)))
                        )
                )
            } while (cursor.moveToNext())
        }
        return stationHistories
    }
}