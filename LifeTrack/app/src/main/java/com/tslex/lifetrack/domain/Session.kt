package com.tslex.lifetrack.domain

import java.sql.Time
import java.util.*

class Session {

    var id: Int = 0
    var wLat: Double
    var wLng: Double
    var creatingTime: Time
    var isWayPointSet: Boolean

    constructor(id: Int, wLat: Double, wLng: Double, creatingTime: Time, isWayPointSet: Boolean) {
        this.id = id
        this.wLat = wLat
        this.wLng = wLng
        this.creatingTime = creatingTime
        this.isWayPointSet = isWayPointSet
    }

    constructor() {
        this.wLat = .0
        this.wLng = .0
        this.creatingTime = Time(Date().time)
        this.isWayPointSet = false
    }
}