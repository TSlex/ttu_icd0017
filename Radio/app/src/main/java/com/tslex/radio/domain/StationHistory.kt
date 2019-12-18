package com.tslex.radio.domain

import java.sql.Date
import java.sql.Time
import java.sql.Timestamp

class StationHistory {

    var id: Int = 0
    var stationId: Int
    var songName: String
    var artistName: String
    var playedCount: Int
    var lastPlayedTime: Timestamp

    constructor(id: Int, songName: String, artistName: String, stationId: Int, playedCount: Int, lastPlayedTime: Timestamp) {
        this.id = id
        this.songName = songName
        this.artistName = artistName
        this.stationId = stationId
        this.playedCount = playedCount
        this.lastPlayedTime = lastPlayedTime
    }

    constructor(songName: String, artistName: String, stationId: Int, playedCount: Int, lastPlayedTime: Timestamp) {
        this.songName = songName
        this.artistName = artistName
        this.stationId = stationId
        this.playedCount = playedCount
        this.lastPlayedTime = lastPlayedTime
    }
}