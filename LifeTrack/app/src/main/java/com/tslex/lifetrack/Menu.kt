package com.tslex.lifetrack

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import com.tslex.lifetrack.repo.PointRepo
import com.tslex.lifetrack.repo.SessionRepo
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat

class Menu : AppCompatActivity() {

    private val file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
    }

    fun onSettingsButtonClicked(view: View){
        val intent = Intent(this, Preferences::class.java)
        startActivity(intent)
    }

    fun onExportButtonClicked(view: View) {

        val sessions = SessionRepo(this).open()
        val session = sessions.getLast()
        sessions.close()

        if (session == null) return

        val points = PointRepo(this).open()
        val pointList = points.getAll(session.id)
        points.close()

        val rPoints = pointList.filter {point -> point.typeId != 1}
        val cPoints = pointList.filter {point -> point.typeId == 1}

        val fileName = "lifetrack.gpx"
        val pathToFile = applicationContext.filesDir.absolutePath + "/" + fileName

        val file = File(pathToFile)
        if (file.exists()){
            file.delete()
        }

        val fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
        val writer = OutputStreamWriter(fileOutputStream)

        writer.write(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<gpx\n" +
                    "xmlns=\"http://www.topografix.com/GPX/1/0\"\n" +
                    "version=\"1.0\"\n" +
                    "creator=\"com.tslex.lifetrack\"\n" +
                    "author=\"Aleksandr Ivanov\"\n" +
                    "time=\"${GPSTools.getTimeInIso(session.creatingTime)}\"\n" +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n"
        )


        for (point in cPoints){
            writer.write("<wpt lat=\"${point.pLat}\" lon=\"${point.pLng}\">")
            writer.write("<time>${GPSTools.getTimeInIso(point.timeOfCreating)}</time>")
            writer.write("</wpt>\n")
        }

        writer.write("<rte>")
        for (point in rPoints){
            writer.write("<rtept lat=\"${point.pLat}\" lon=\"${point.pLng}\">")
                    writer.write("<time>${GPSTools.getTimeInIso(point.timeOfCreating)}</time>")
                    writer.write("</rtept>\n")
        }
        writer.write("</rte>")

        writer.write("</gpx>")

        writer.close()
        fileOutputStream.close()

        val path = FileProvider.getUriForFile(this, "com.tslex.lifetrack.provider", file)

        val sdf = SimpleDateFormat("HH:mm:ss z")

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "*/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, path)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Exported GPX Data")
        shareIntent.putExtra(Intent.EXTRA_TEXT, "" +
                "Session time: ${sdf.format(session.creatingTime)}\n" +
                "By: Lifetrack\n")

        val chooser = Intent.createChooser(shareIntent, "Sharing gpx data")

        startActivity(chooser)

        val fileInput = FileInputStream(file)
        val reader = InputStreamReader(fileInput)

        Log.d("file", reader.readText())
    }
}
