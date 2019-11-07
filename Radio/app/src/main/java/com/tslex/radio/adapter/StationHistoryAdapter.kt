package com.tslex.radio.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.tslex.radio.R
import com.tslex.radio.domain.StationHistory
import com.tslex.radio.repo.HistoryRepo
import kotlinx.android.synthetic.main.station_history_item.view.*

class StationHistoryAdapter (context: Context, private val repo: HistoryRepo) :
    RecyclerView.Adapter<StationHistoryAdapter.ViewHolder>(){

    private var dataset: ArrayList<StationHistory> = repo.getAll()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    fun refreshData(){
        dataset = repo.getAll()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView = inflater.inflate(R.layout.station_history_item, parent, false)
        return ViewHolder(rowView)
    }

    override fun getItemCount(): Int {
        return dataset.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Log.d("Song binded", "OK")

        val history = dataset.get(position)
        holder.itemView.songName.text = history.songName
        holder.itemView.artistName.text = history.artistName
        holder.itemView.playCount.text = history.playedCount.toString()
        holder.itemView.lastPlayed.text = history.lastPlayedTime.toString()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}