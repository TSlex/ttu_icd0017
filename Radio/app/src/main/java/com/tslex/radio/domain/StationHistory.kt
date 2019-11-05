package com.tslex.radio.domain

class StationHistory {

    var id: Int = 0
    var stationId: Int
    var songName: String
    var artistName: String

    constructor(id: Int, songName: String, artistName: String, stationId: Int) {
        this.id = id
        this.songName = songName
        this.artistName = artistName
        this.stationId = stationId
    }

    constructor(songName: String, artistName: String, stationId: Int) {
        this.songName = songName
        this.artistName = artistName
        this.stationId = stationId
    }
}