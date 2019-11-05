package com.tslex.dbdemo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DbHelper (context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private val TAG = this::class.java.canonicalName

        const val DATABASE_NAME = "appdb.db"
        const val DATABASE_VERSION = 1

        const val PERSON_TABLE_NAME = "PERSON"

        const val PERSON_ID = "_id"
        const val PERSON_FIRSTNAME = "firstName"
        const val PERSON_LASTNAME = "lastName"

        const val SQL_PERSON_CREATE_TABLE =
            "CREATE TABLE $PERSON_TABLE_NAME " +
                    "(" +
                    "$PERSON_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$PERSON_FIRSTNAME TEXT NOT NULL, " +
                    "$PERSON_LASTNAME TEXT NOT NULL" +
                    ")"
        const val SQL_DELETE_TABLES = "DROP TABLE IF EXISTS " + PERSON_TABLE_NAME
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG, "onCreate")
        db?.execSQL(SQL_PERSON_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onDestroy")
        db?.execSQL(SQL_DELETE_TABLES)
        onCreate(db)
    }
}