package com.tslex.lifetrack.domain

import java.sql.Time
import java.sql.Timestamp
import java.util.*

class Point {
    var id: Int = 0
    var sessionId: Int
    var typeId: Int
    var pLat: Double
    var pLng: Double
    var timeOfCreating: Timestamp

    constructor(
        id: Int,
        sessionId: Int,
        typeId: Int,
        pLat: Double,
        pLng: Double,
        timeOfCreating: Timestamp
    ) {
        this.id = id
        this.sessionId = sessionId
        this.typeId = typeId
        this.pLat = pLat
        this.pLng = pLng
        this.timeOfCreating = timeOfCreating
    }

    constructor(sessionId: Int, typeId: Int, pLat: Double, pLng: Double) {
        this.sessionId = sessionId
        this.typeId = typeId
        this.pLat = pLat
        this.pLng = pLng
        this.timeOfCreating = Timestamp(Date().time)
    }
}