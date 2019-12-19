package com.tslex.lifetrack.domain

import java.sql.Time
import java.sql.Timestamp
import java.util.*

class Session {

    var id: Int = 0
    var wLat: Double
    var wLng: Double
    var creatingTime: Timestamp
    var isWayPointSet: Boolean

    var sessionTime: String = "00:00:00"

    var paceStart: String = "00:00:00"
    var paceCp: String = "00:00:00"
    var paceWp: String = "00:00:00"

    var dirDistStart: Int = 0
    var dirDirCp: Int = 0
    var dirDirWp: Int = 0

    var calDirStart: Int = 0
    var calDirCp: Int = 0
    var calDirWp: Int = 0


    constructor(id: Int, wLat: Double, wLng: Double, creatingTime: Timestamp, isWayPointSet: Boolean) {
        this.id = id
        this.wLat = wLat
        this.wLng = wLng
        this.creatingTime = creatingTime
        this.isWayPointSet = isWayPointSet
    }

    constructor() {
        this.wLat = .0
        this.wLng = .0
        this.creatingTime = Timestamp(Date().time)
        this.isWayPointSet = false
    }

//    constructor(
//        id: Int,
//        wLat: Double,
//        wLng: Double,
//        creatingTime: Timestamp,
//        isWayPointSet: Boolean,
//        sessionTime: String,
//        paceStart: String,
//        paceCp: String,
//        paceWp: String,
//        dirDistStart: Int,
//        dirDirCp: Int,
//        dirDirWp: Int,
//        calDirStart: Int,
//        calDirCp: Int,
//        calDirWp: Int
//    ) {
//        this.id = id
//        this.wLat = wLat
//        this.wLng = wLng
//        this.creatingTime = creatingTime
//        this.isWayPointSet = isWayPointSet
//        this.sessionTime = sessionTime
//        this.paceStart = paceStart
//        this.paceCp = paceCp
//        this.paceWp = paceWp
//        this.dirDistStart = dirDistStart
//        this.dirDirCp = dirDirCp
//        this.dirDirWp = dirDirWp
//        this.calDirStart = calDirStart
//        this.calDirCp = calDirCp
//        this.calDirWp = calDirWp
//    }
}