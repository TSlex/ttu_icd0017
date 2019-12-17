package com.tslex.lifetrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.tslex.lifetrack.repo.PointRepo
import com.tslex.lifetrack.repo.SessionRepo
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class Menu : AppCompatActivity() {

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

        val file = FileOutputStream(session.creatingTime.time.toString() + ".gpx")
        val writer = OutputStreamWriter(file)

        writer.write(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<gpx\n" +
                    "xmlns=\"http://www.topografix.com/GPX/1/0\"\n" +
                    "version=\"1.0\"\n" +
                    "creator=\"com.tslex.lifetrack\"\n" +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n"
        )

    }
}
