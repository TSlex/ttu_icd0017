package com.tslex.radio.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import java.io.ByteArrayOutputStream

class RadioStation {

    var id: Int = 0
    var stationName: String
    var stationImage: String = ""
    var stationMeta: String
    var stationStream: String
    var stationHistory : MutableList<StationHistory> = arrayListOf()

    constructor(id: Int, stationName: String, stationImage: String, stationMeta: String, stationStream: String) {
        this.id = id
        this.stationName = stationName
        this.stationImage = stationImage
        this.stationMeta = stationMeta
        this.stationStream = stationStream
    }

    constructor(stationName: String, stationMeta: String, stationStream: String) {
        this.stationName = stationName
        this.stationMeta = stationMeta
        this.stationStream = stationStream
    }

    fun getImage() : Bitmap{

        var byteArray : ByteArray

        var tempList: MutableList<Byte> = arrayListOf()

        for (byte in stationImage.split(", ")) {
            tempList.add(byte.toByte())
        }

        byteArray = tempList.toByteArray()

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.count())
    }

    fun convertBitmap(bitmap: Bitmap){

        var stream: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
        stationImage = stream.toByteArray().joinToString(", ")
    }
}