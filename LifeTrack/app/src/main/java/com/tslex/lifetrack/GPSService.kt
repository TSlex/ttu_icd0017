package com.tslex.lifetrack

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.GoogleMap
import com.tslex.lifetrack.domain.Session
import com.tslex.lifetrack.repo.SessionRepo
import kotlin.math.log

class GPSService : Service(), LocationListener, GpsStatus.Listener {

    private val TAG = this::class.java.simpleName
    private val broadcastReceiver = ServiceBroadcastReceiver()

    private var trackState = TrackState.STOPED

    private lateinit var locationManager: LocationManager
    private lateinit var locationProvider: String
    private lateinit var criteria: Criteria

    private var currentSessionId: Int = 0

    override fun onCreate() {
        super.onCreate()

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intents.INTENT_TRACKING_PAUSE.getAction())
        intentFilter.addAction(Intents.INTENT_TRACKING_RESUME.getAction())
        intentFilter.addAction(Intents.INTENT_TRACKING_STOP.getAction())

        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(broadcastReceiver, intentFilter)

        //setup location manager and provider
        setUpLocations()
    }

    private fun setUpLocations() {
        criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_HIGH
        criteria.isAltitudeRequired = false
        criteria.isSpeedRequired = true
        criteria.isCostAllowed = false
        criteria.isBearingRequired = true
        criteria.horizontalAccuracy = Criteria.ACCURACY_HIGH
        criteria.verticalAccuracy = Criteria.ACCURACY_HIGH

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProvider = locationManager.getBestProvider(criteria, true)!!
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        val sessions = SessionRepo(this).open()
        sessions.add(Session())
        currentSessionId = sessions.getLast()!!.id
        sessions.close()

        startListener()

        return START_STICKY
    }

    private fun startListener() {
        if (trackState == TrackState.STARTED) return

        try {
            locationManager.requestLocationUpdates(2000, 0.1f, criteria, this, null)
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

    override fun onGpsStatusChanged(p0: Int) {
    }



    private inner class ServiceBroadcastReceiver : BroadcastReceiver() {

        private val TAG = this::class.java.simpleName

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive")

            val action = intent?.action

            when (action) {
                Intents.INTENT_TRACKING_RESUME.getAction() -> {
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
