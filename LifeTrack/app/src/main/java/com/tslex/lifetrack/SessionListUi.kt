package com.tslex.lifetrack

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tslex.lifetrack.repo.PointRepo
import com.tslex.lifetrack.repo.SessionRepo
import kotlinx.android.synthetic.main.activity_session_list_ui.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat

class SessionListUi : AppCompatActivity() {

    private val TAG = this::class.java.canonicalName

    private lateinit var sessions: SessionRepo
    private lateinit var adapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_list_ui)

        Log.d(TAG, "onCreate")

        sessions = SessionRepo(this).open()
        sessionList.layoutManager = LinearLayoutManager(this)

        adapter = SessionAdapter(this, sessions)
        sessionList.adapter = adapter

        sessions.close()
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "onDestroy")
    }

    fun buttonCloseClicked(view: View){
        this.finish()
    }

    fun buttonUpdateClicked(view: View){
        update()
    }

    fun update(){
        adapter.notifyDataSetChanged()
    }

    fun delete(sessionId: Int){
        sessions = SessionRepo(this).open()
        sessions.delete(sessionId)
        sessions.close()
    }

    fun view(sessionId: Int){
        val intent = Intent(Intents.INTENT_LOAD_SESSION.getAction())
        intent.putExtra("sessionId", sessionId)
        setResult(13, intent)
        finish()
    }

    fun export(sessionId: Int){
        val sessions = SessionRepo(this).open()
        val session = sessions.getById(sessionId)
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
                    "time=\"${AssistTools.getTimeInIso(session.creatingTime)}\"\n" +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n"
        )


        for (point in cPoints){
            writer.write("<wpt lat=\"${point.pLat}\" lon=\"${point.pLng}\">")
            writer.write("<time>${AssistTools.getTimeInIso(point.timeOfCreating)}</time>")
            writer.write("</wpt>\n")
        }

        writer.write("<rte>")
        for (point in rPoints){
            writer.write("<rtept lat=\"${point.pLat}\" lon=\"${point.pLng}\">")
            writer.write("<time>${AssistTools.getTimeInIso(point.timeOfCreating)}</time>")
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
        shareIntent.putExtra(
            Intent.EXTRA_TEXT, "" +
                    "Session time: ${sdf.format(session.creatingTime)}\n" +
                    "By: Lifetrack\n")

        val chooser = Intent.createChooser(shareIntent, "Sharing gpx data")

        startActivity(chooser)

        val fileInput = FileInputStream(file)
        val reader = InputStreamReader(fileInput)

        Log.d("file", reader.readText())
    }


}
