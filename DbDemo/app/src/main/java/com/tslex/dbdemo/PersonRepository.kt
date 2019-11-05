package com.tslex.dbdemo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.lang.reflect.Array

class PersonRepository(val context: Context) {

    private var TAG = this::class.java.canonicalName
    private lateinit var dbHelper: DbHelper
    private lateinit var db: SQLiteDatabase

    fun open(): PersonRepository {
        Log.d(TAG, "open")

        dbHelper = DbHelper(context)
        db = dbHelper.writableDatabase
        return this
    }

    fun erase() {
        db.execSQL(DbHelper.SQL_DELETE_TABLES)
        db.execSQL(DbHelper.SQL_PERSON_CREATE_TABLE)
//        db.execSQL("DELETE FROM ${DbHelper.PERSON_TABLE_NAME} WHERE ${DbHelper.PERSON_ID}>0")
    }

    fun close() {
        Log.d(TAG, "close")

        dbHelper.close()
    }

    fun add(person: Person) {
        Log.d(TAG, "add")

        val contentValue = ContentValues()
        contentValue.put(DbHelper.PERSON_FIRSTNAME, person.firstName)
        contentValue.put(DbHelper.PERSON_LASTNAME, person.lastNamae)
        db.insert(DbHelper.PERSON_TABLE_NAME, null, contentValue)
    }

    private fun fetch(): Cursor {
        Log.d(TAG, "fetch")

        val columns = arrayOf(
            DbHelper.PERSON_ID,
            DbHelper.PERSON_FIRSTNAME,
            DbHelper.PERSON_LASTNAME
        )

        val cursor = db.query(
            DbHelper.PERSON_TABLE_NAME,
            columns,
            null,
            null,
            null,
            null,
            null
        )

        return cursor
    }

    fun getAll(): ArrayList<Person> {

        Log.d(TAG, "getAll")

        val persons = ArrayList<Person>()

        Log.d(TAG, "fetching...")
        val cursor = fetch()

        Log.d(TAG, "counting...")

        if (cursor.count > 0) {

            Log.d(TAG, "moving...")
            cursor.moveToFirst()

            do {
                Log.d(TAG, "add...")
                persons.add(
                    Person(
                        cursor.getInt(cursor.getColumnIndex(DbHelper.PERSON_ID)),
                        cursor.getString(cursor.getColumnIndex(DbHelper.PERSON_FIRSTNAME)),
                        cursor.getString(cursor.getColumnIndex(DbHelper.PERSON_LASTNAME))
                    )
                )
            } while (cursor.moveToNext())
        }



        return persons
    }
}