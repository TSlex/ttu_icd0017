package com.tslex.radio.adapter

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tslex.radio.R
import com.tslex.radio.domain.RadioStation
import com.tslex.radio.repo.RadioRepo

class RadioStationAdapter(context: Context, textView: Int, values: ArrayList<RadioStation>) :
        ArrayAdapter<RadioStation>(context, textView, values){

    override fun getView(position: Int, _convertView: View?, parent: ViewGroup): View {
//        return super.getView(position, convertView, parent)

        var station: RadioStation = getItem(position)!!
        var convertView = _convertView

        if (convertView == null){
            convertView = LayoutInflater.from(context)!!.inflate(R.layout.radio_station_spinner_element, null)
        }

        (convertView!!.findViewById(R.id.stationId) as TextView).text = station.id.toString()
        (convertView!!.findViewById(R.id.stationName) as TextView).text = station.stationName

        return convertView
    }

    override fun getDropDownView(position: Int, _convertView: View?, parent: ViewGroup): View {

        var station: RadioStation = getItem(position)!!
        var convertView = _convertView

        if (convertView == null){
            convertView = LayoutInflater.from(context)!!.inflate(R.layout.radio_station_spinner_element, null)
        }

        (convertView!!.findViewById(R.id.stationId) as TextView).text = station.id.toString()
        (convertView!!.findViewById(R.id.stationName) as TextView).text = station.stationName

        return convertView
    }
}