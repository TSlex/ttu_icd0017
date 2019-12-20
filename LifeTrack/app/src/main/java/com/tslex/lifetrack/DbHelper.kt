package com.tslex.lifetrack

import android.content.ContentValues.TAG
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
    null
) {
    companion object {
        const val DATABASE_NAME = "appdb.db"
        const val DATABASE_VERSION = 1

        //Session
        const val SESSION_TABLE_NAME = "SESSION"
        const val SESSION_ID = "_id"
        const val SESSION_WAYPOINT_LAT = "waypoint_latitude"
        const val SESSION_WAYPOINT_LNG = "waypoint_longitude"
        const val SESSION_CREATING_TIME = "creating_time"
        const val SESSION_WAYPOINT_SET = "is_waypoint_set"

        //Session Data
        const val SESSION_TOTAL_TIME = "total_time"
        const val SESSION_PACE_START = "pace_start"
        const val SESSION_PACE_CP = "pace_checkpoint"
        const val SESSION_PACE_WP = "pace_waypoint"
        const val SESSION_DDIST_START = "direct_distance_from_start"
        const val SESSION_DDIST_CP = "direct_distance_from_checkpoint"
        const val SESSION_DDIST_WP = "direct_distance_from_waypoint"
        const val SESSION_CDIST_START = "calculated_distance_from_start"
        const val SESSION_CDIST_CP = "calculated_distance_from_checkpoint"
        const val SESSION_CDIST_WP = "calculated_distance_from_waypoint"

        //Point
        const val POINT_TABLE_NAME = "POINT"
        const val POINT_ID = "_id"
        const val POINT_SESSION_ID = "session_id"
        const val POINT_PTYPE_ID = "type_id"
        const val POINT_LAT = "lat"
        const val POINT_LNG = "lng"
        const val POINT_CREATING_TIME = "creating_time"

        //PType
        const val POINT_TYPE_TABLE_NAME = "POINT_TYPE"
        const val POINT_TYPE_ID = "_id"
        const val POINT_TYPE = "type"

        //Constructor
        const val SQL_CREATE_SESSION_TABLE =
            "CREATE TABLE IF NOT EXISTS $SESSION_TABLE_NAME " +
                    "(" +
                    "$SESSION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$SESSION_WAYPOINT_LAT REAL NOT NULL, " +
                    "$SESSION_WAYPOINT_LNG REAL NOT NULL, " +
                    "$SESSION_CREATING_TIME DATE NOT NULL, " +
                    "$SESSION_WAYPOINT_SET INTEGER NOT NULL, " +

                    "$SESSION_TOTAL_TIME TEXT NOT NULL, " +
                    "$SESSION_PACE_START TEXT NOT NULL, " +
                    "$SESSION_PACE_CP TEXT NOT NULL, " +
                    "$SESSION_PACE_WP TEXT NOT NULL, " +
                    "$SESSION_DDIST_START INTEGER NOT NULL, " +
                    "$SESSION_DDIST_CP INTEGER NOT NULL, " +
                    "$SESSION_DDIST_WP INTEGER NOT NULL, " +
                    "$SESSION_CDIST_START INTEGER NOT NULL, " +
                    "$SESSION_CDIST_CP INTEGER NOT NULL, " +
                    "$SESSION_CDIST_WP INTEGER NOT NULL" +
                    ")"

        const val SQL_CREATE_POINT_TABLE =
            "CREATE TABLE IF NOT EXISTS $POINT_TABLE_NAME " +
                    "(" +
                    "$POINT_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$POINT_SESSION_ID INTEGER NOT NULL, " +
                    "$POINT_PTYPE_ID INTEGER NOT NULL, " +
                    "$POINT_LAT REAL NOT NULL, " +
                    "$POINT_LNG REAL NOT NULL, " +
                    "$POINT_CREATING_TIME DATE NOT NULL" +
                    ")"
        const val SQL_CREATE_POINT_TYPE_TABLE =
            "CREATE TABLE IF NOT EXISTS $POINT_TYPE_TABLE_NAME " +
                    "(" +
                    "$POINT_TYPE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$POINT_TYPE TEXT NOT NULL" +
                    ")"

        //Destructor
        const val SQL_DROP_SESSION_TABLE = "DROP TABLE IF EXISTS $SESSION_TABLE_NAME"
        const val SQL_DROP_POINT_TABLE = "DROP TABLE IF EXISTS $POINT_TABLE_NAME"
        const val SQL_DROP_POINT_TYPE_TABLE = "DROP TABLE IF EXISTS $POINT_TYPE_TABLE_NAME"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG, "onCreate")
        db?.execSQL(SQL_CREATE_SESSION_TABLE)
        db?.execSQL(SQL_CREATE_POINT_TABLE)
        db?.execSQL(SQL_CREATE_POINT_TYPE_TABLE)
        init(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpdate")
        db?.execSQL(SQL_DROP_SESSION_TABLE)
        db?.execSQL(SQL_DROP_POINT_TABLE)
        db?.execSQL(SQL_DROP_POINT_TYPE_TABLE)
        onCreate(db)
    }

    private fun init(db: SQLiteDatabase?){
        db?.execSQL("INSERT INTO $POINT_TYPE_TABLE_NAME ($POINT_TYPE_ID, $POINT_TYPE) VALUES (1, 'checkpoint')")
        db?.execSQL("INSERT INTO $POINT_TYPE_TABLE_NAME ($POINT_TYPE_ID, $POINT_TYPE) VALUES (2, 'route_walking')")
//        db?.execSQL("INSERT INTO $POINT_TYPE_TABLE_NAME ($POINT_TYPE_ID, $POINT_TYPE) VALUES (3, 'route_running')")
//        db?.execSQL("INSERT INTO $POINT_TYPE_TABLE_NAME ($POINT_TYPE_ID, $POINT_TYPE) VALUES (4, 'route_to_fast')")
    }
}