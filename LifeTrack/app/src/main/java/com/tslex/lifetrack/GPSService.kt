package com.tslex.lifetrack

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlin.math.log

class GPSService : Service(), LocationListener {

    private val TAG = this::class.java.simpleName
    private val broadcastReceiver = ServiceBroadcastReceiver()

    private var trackState = TrackState.STOPED

    private lateinit var locationManager: LocationManager
    private lateinit var locationProvider: String
    private lateinit var criteria: Criteria

    override fun onCreate() {
        super.onCreate()

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intents.INTENT_TRACKING_START.getAction())
        intentFilter.addAction(Intents.INTENT_TRACKING_PAUSE.getAction())
        intentFilter.addAction(Intents.INTENT_TRACKING_STOP.getAction())

        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(broadcastReceiver, intentFilter)

        criteria = Criteria()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProvider = locationManager.getBestProvider(criteria, false)!!
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        startListener()

        return START_STICKY
    }

    private fun startListener() {
        if (trackState == TrackState.STARTED) return

        try {
            locationManager.requestLocationUpdates(locationProvider, 100, 1f, this)
        } catch (e: SecurityException) {
            Log.e("MAIN", e.toString())
        }
        trackState = TrackState.STARTED
    }

    private fun stopListener() {
        locationManager.removeUpdates(this)
        trackState = TrackState.PAUSED
    }

    override fun onLocationChanged(location: Location?) {
        val intent = Intent(Intents.INTENT_UI_UPDATE.getAction())
        intent.putExtra("lat", location!!.latitude)
        intent.putExtra("lng", location!!.longitude)

        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(intent)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    private inner class ServiceBroadcastReceiver : BroadcastReceiver() {

        private val TAG = this::class.java.simpleName

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive")

            val action = intent?.action

            when (action) {
                Intents.INTENT_TRACKING_START.getAction() -> {
                    startListener()
                }
                Intents.INTENT_TRACKING_PAUSE.getAction() -> {
                    stopListener()
                }
                Intents.INTENT_TRACKING_STOP.getAction() -> {
                    stopListener()
                }
                else -> return
            }
        }
    }
}
