package com.tslex.radio.domain

import java.sql.Time

class StationHistory {

    var id: Int = 0
    var stationId: Int
    var songName: String
    var artistName: String
    var playedCount: Int
    var lastPlayedTime: Time

    constructor(id: Int, songName: String, artistName: String, stationId: Int, playedCount: Int, lastPlayedTime: Time) {
        this.id = id
        this.songName = songName
        this.artistName = artistName
        this.stationId = stationId
        this.playedCount = playedCount
        this.lastPlayedTime = lastPlayedTime
    }

    constructor(songName: String, artistName: String, stationId: Int, playedCount: Int, lastPlayedTime: Time) {
        this.songName = songName
        this.artistName = artistName
        this.stationId = stationId
        this.playedCount = playedCount
        this.lastPlayedTime = lastPlayedTime
    }
}