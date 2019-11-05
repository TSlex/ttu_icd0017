package com.tslex.radio.adapter

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tslex.radio.domain.RadioStation
import com.tslex.radio.repo.RadioRepo

class RadioStationAdapter (context: Context, private val repo: RadioRepo)