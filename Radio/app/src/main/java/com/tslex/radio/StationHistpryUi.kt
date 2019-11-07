package com.tslex.radio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tslex.radio.adapter.StationHistoryAdapter
import com.tslex.radio.repo.HistoryRepo
import kotlinx.android.synthetic.main.station_histpry_ui.*

class StationHistpryUi : AppCompatActivity() {

    private val TAG = this::class.java.canonicalName

    private lateinit var historyRepository: HistoryRepo
    private lateinit var adapter: RecyclerView.Adapter<*>
    private var currentStation: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.station_histpry_ui)

        Log.d(TAG, "onCreate")
        currentStation = intent.getIntExtra("station_id", -1)
        Log.d(TAG, currentStation.toString())

        historyRepository = HistoryRepo(this).open()
        historyView.layoutManager = LinearLayoutManager(this)
        adapter = StationHistoryAdapter(this, historyRepository, currentStation)
        historyView.adapter = adapter

    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "onDestroy")

        historyRepository.close()
    }

    fun close(view: View){
        this.finish()
    }
}
