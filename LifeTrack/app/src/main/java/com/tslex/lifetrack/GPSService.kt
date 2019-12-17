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
import com.tslex.lifetrack.domain.PType
import com.tslex.lifetrack.domain.Point
import com.tslex.lifetrack.domain.Session
import com.tslex.lifetrack.repo.PTypeRepo
import com.tslex.lifetrack.repo.PointRepo
import com.tslex.lifetrack.repo.SessionRepo
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.log

class GPSService : Service(), LocationListener, GpsStatus.Listener {

    private val TAG = this::class.java.simpleName
    private val broadcastReceiver = ServiceBroadcastReceiver()

    private lateinit var locationManager: LocationManager
    private lateinit var locationProvider: String
    private lateinit var criteria: Criteria
    private lateinit var thread: ScheduledExecutorService

    private lateinit var currentSession: Session

    private var lastLocation: Location? = null
    private var firstLocation: Location? = null
    private var lastCp: Point? = null
//    private var lastWp: Point? = null

    private lateinit var pointTypes: ArrayList<PType>

    override fun onCreate() {
        super.onCreate()

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intents.INTENT_TRACKING_PAUSE.getAction())
        intentFilter.addAction(Intents.INTENT_TRACKING_RESUME.getAction())
        intentFilter.addAction(Intents.INTENT_TRACKING_STOP.getAction())
        intentFilter.addAction(Intents.INTENT_ADD_WP.getAction())
        intentFilter.addAction(Intents.INTENT_ADD_CP.getAction())

        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(broadcastReceiver, intentFilter)

        //setup location manager and provider
        setUpLocations()
    }

    private fun setUpLocations() {
//        criteria = Criteria()
//        criteria.accuracy = Criteria.ACCURACY_FINE
//        criteria.powerRequirement = Criteria.POWER_HIGH
//        criteria.isAltitudeRequired = false
//        criteria.isSpeedRequired = true
//        criteria.isCostAllowed = false
//        criteria.isBearingRequired = true
//        criteria.horizontalAccuracy = Criteria.ACCURACY_HIGH
//        criteria.verticalAccuracy = Criteria.ACCURACY_HIGH

        criteria = GPSTools.getCriteria()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProvider = locationManager.getBestProvider(criteria, true)!!
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        //init current session
        val sessions = SessionRepo(this).open()
        sessions.add(Session())
        currentSession = sessions.getLast()!!
        sessions.close()

        val pTypes = PTypeRepo(this).open()
        pointTypes = pTypes.getAll()
        pTypes.close()

        startListener()

        thread = Executors.newScheduledThreadPool(1)
        thread.scheduleAtFixedRate({

            val meta = Intent(Intents.INTENT_UI_UPDATE_META.getAction())

            if (firstLocation != null && lastLocation != null){
                meta.putExtra("dirDirStart",
                    GPSTools.getDistance(
                        firstLocation!!.latitude,
                        firstLocation!!.longitude,
                        lastLocation!!.latitude,
                        lastLocation!!.longitude))
            }

            if (lastCp != null && lastLocation != null){
                meta.putExtra("dirDirCp",
                    GPSTools.getDistance(
                        lastCp!!.pLat,
                        lastCp!!.pLng,
                        lastLocation!!.latitude,
                        lastLocation!!.longitude))
            }

            if (currentSession.isWayPointSet && lastLocation != null){
                meta.putExtra("dirDirWp",
                    GPSTools.getDistance(
                        currentSession!!.wLat,
                        currentSession!!.wLng,
                        lastLocation!!.latitude,
                        lastLocation!!.longitude))
            }

//            meta.putExtra("calDirStart", )
//            meta.putExtra("calDirCp", )
//            meta.putExtra("calDirWp", )

//            meta.putExtra("paceStart", )
//            meta.putExtra("paceCp", )
//            meta.putExtra("paceWp", )

//            meta.putExtra("totalTime", Date().time - currentSession.creatingTime.time)


            LocalBroadcastManager.getInstance(applicationContext)
                .sendBroadcast(meta)

        }, 0, 1, TimeUnit.SECONDS)

        return START_STICKY
    }

    private fun startListener() {
        try {
            locationManager.requestLocationUpdates(2000, 0.1f, criteria, this, null)
        } catch (e: SecurityException) {
            Log.e("MAIN", e.toString())
        }
    }

    private fun stopListener() {
        locationManager.removeUpdates(this)
    }

    private fun addCp(){
        if (lastLocation == null) return

        val tmp = lastLocation!!

        val points = PointRepo(this).open()
        lastCp = Point(currentSession.id, 1, tmp.latitude, tmp.longitude)
        points.add(lastCp!!)

        val intent = Intent(Intents.INTENT_UI_PLACE_CP.getAction())
        intent.putExtra("lat", tmp.latitude)
        intent.putExtra("lng", tmp.longitude)

        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(intent)
    }

    private fun addWp(latitude: Double, longitude: Double){

        val sessions = SessionRepo(this).open()
        currentSession.isWayPointSet = true
        currentSession.wLat = latitude
        currentSession.wLng = longitude

        Log.d(TAG, latitude.toString())
        Log.d(TAG, longitude.toString())

        sessions.update(currentSession)
        sessions.close()
//        val intent = Intent(Intents.INTENT_UI_PLACE_WP.getAction())
//        intent.putExtra("lat", tmp.latitude)
//        intent.putExtra("lng", tmp.longitude)
//
//        LocalBroadcastManager.getInstance(applicationContext)
//            .sendBroadcast(intent)
    }

    override fun onLocationChanged(location: Location?) {
        val intent = Intent(Intents.INTENT_UI_UPDATE_LOCATION.getAction())
        intent.putExtra("lat", location!!.latitude)
        intent.putExtra("lng", location!!.longitude)

        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(intent)

        val points = PointRepo(this).open()
        points.add(Point(currentSession.id, 2, location!!.latitude, location!!.longitude))

        if (firstLocation == null) firstLocation = location
        lastLocation = location
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
                Intents.INTENT_ADD_WP.getAction() -> {
                    val latitude = intent.getDoubleExtra("lat", .0)
                    val longitude = intent.getDoubleExtra("lng", .0)
                    addWp(latitude, longitude)
                }
                Intents.INTENT_ADD_CP.getAction() -> {
                    addCp()
                }
                else -> return
            }
        }
    }
}
