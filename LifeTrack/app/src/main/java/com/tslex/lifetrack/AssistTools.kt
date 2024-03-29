package com.tslex.lifetrack

import android.location.Criteria
import android.util.Log
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*


class AssistTools {
    companion object{
        const val R = 6371e3

        fun getDirectDistance(lat_1: Double, lng_1: Double, lat_2: Double, lng_2: Double): Int{
            val f1 = Math.toRadians(lat_1)
            val f2 = Math.toRadians(lat_1)
            val df = Math.toRadians(lat_2-lat_1)
            val dh = Math.toRadians(lng_2-lng_1)

            val a = sin(df / 2).pow(2) + cos(f1) * cos(f2) * sin(dh / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1-a))

            return ceil(c * R).toInt()
        }

        fun getTimeBetween(time1: Timestamp, time2: Timestamp): Long{
            return abs(time1.time - time2.time)
        }

        fun formatRawTime(rawTime: Long) : String{
            val sdf = SimpleDateFormat("HH:mm:ss")
            sdf.timeZone = TimeZone.getTimeZone("GMT")

            return sdf.format(Date(rawTime))
        }

        fun getPace(rawTime: Long, distance: Int): String{

            val distKm: Double = (distance.toDouble() / 1000)

            if (distKm <= .0){
                return "00:00:00"
            }

            return formatRawTime((rawTime.toDouble() / distKm).roundToLong())
        }

        fun getTimeInIso(time: Timestamp): String{
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:S'Z'").format(Date(time.time))
        }

        fun getCriteria(): Criteria{
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            criteria.powerRequirement = Criteria.POWER_HIGH
            criteria.isAltitudeRequired = false
            criteria.isSpeedRequired = true
            criteria.isCostAllowed = false
            criteria.isBearingRequired = true
            criteria.horizontalAccuracy = Criteria.ACCURACY_HIGH
            criteria.verticalAccuracy = Criteria.ACCURACY_HIGH

            return criteria
        }
    }
}