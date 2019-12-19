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
}