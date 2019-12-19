package com.tslex.lifetrack

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.util.TimeUtils
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.tslex.lifetrack.domain.PType
import com.tslex.lifetrack.domain.Point
import com.tslex.lifetrack.domain.Session
import com.tslex.lifetrack.repo.PTypeRepo
import com.tslex.lifetrack.repo.PointRepo
import com.tslex.lifetrack.repo.SessionRepo
import java.lang.Exception
import java.sql.Time
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

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

        registerReceiver(broadcastReceiver, intentFilter)

        //setup location manager and provider
        setUpLocations()

        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()

        stopListener()
        NotificationManagerCompat.from(this).cancel(0)
    }

    private fun setUpLocations() {
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

        return START_STICKY
    }

    private fun startListener() {
        try {
            locationManager.requestLocationUpdates(2000, 0.1f, criteria, this, null)
        } catch (e: SecurityException) {
            Log.e("MAIN", e.toString())
        }

        startMetaUpdating()
    }

    private fun stopListener() {
        locationManager.removeUpdates(this)

        stopMetaUpdating()
    }

    private fun addCp(){
        if (lastLocation == null) return

        val tmp = lastLocation!!

        val points = PointRepo(this).open()
        lastCp = Point(currentSession.id, 1, tmp.latitude, tmp.longitude)
        points.add(lastCp!!)

        addRp(tmp)

        val intent = Intent(Intents.INTENT_UI_PLACE_CP.getAction())
        intent.putExtra("lat", tmp.latitude)
        intent.putExtra("lng", tmp.longitude)

        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(intent)
    }

    private fun addWp(){

        if (lastLocation == null) return

        val tmp = lastLocation!!

        val sessions = SessionRepo(this).open()
        currentSession.isWayPointSet = true
        currentSession.wLat = tmp.latitude
        currentSession.wLng = tmp.longitude
        sessions.update(currentSession)
        sessions.close()

        addRp(tmp)

        val intent = Intent(Intents.INTENT_UI_PLACE_WP.getAction())
        intent.putExtra("lat", tmp.latitude)
        intent.putExtra("lng", tmp.longitude)

        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(intent)
    }

    private fun addRp(location: Location): Point{
        val intent = Intent(Intents.INTENT_UI_UPDATE_LOCATION.getAction())
        intent.putExtra("lat", location.latitude)
        intent.putExtra("lng", location.longitude)

        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(intent)

        val points = PointRepo(this).open()
        val point = Point(currentSession.id, 2, location!!.latitude, location!!.longitude)
        points.add(point)

        if (firstLocation == null) firstLocation = location
        lastLocation = location

        return point
    }

    private fun calculateDistance(lat: Double, lng: Double): Int{
        if (lastLocation == null) return 0

        val points = PointRepo(this).open()
        val pointList = points.getAll(currentSession.id)
        pointList.reverse()
        points.close()

        var sum = 0
        var lastPoint: Point? = null

        for (p in pointList){
            if (p.pLat == lat && p.pLng == lng){
                if (lastPoint != null){
                    sum += GPSTools.getDirectDistance(lastPoint.pLat, lastPoint.pLng, p.pLat, p.pLng)
                }
                break
            }
            if (lastPoint == null){
//                sum += GPSTools.getDirectDistance(lastLocation!!.latitude, lastLocation!!.longitude, p.pLat, p.pLng)
                lastPoint = p
                continue
            }
            else{
                sum += GPSTools.getDirectDistance(lastPoint.pLat, lastPoint.pLng, p.pLat, p.pLng)
                lastPoint = p
            }
        }

        return sum
    }

    private fun createNotificationChannel() {
        // when on 8 Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "com.tslex.lifetrack.notify",
                "Controller",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Notification with session data and controls"

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startMetaUpdating(){
        thread = Executors.newScheduledThreadPool(1)
        thread.scheduleAtFixedRate({

            val meta = Intent(Intents.INTENT_UI_UPDATE_META.getAction())
            val totalTime = GPSTools.getTimeBetween(Timestamp(Date().time), currentSession.creatingTime)

            meta.putExtra("totalTime", GPSTools.formatRawTime(totalTime))

            if (firstLocation != null && lastLocation != null){
                meta.putExtra("dirDirStart",
                    GPSTools.getDirectDistance(
                        firstLocation!!.latitude,
                        firstLocation!!.longitude,
                        lastLocation!!.latitude,
                        lastLocation!!.longitude))

                val calDist = calculateDistance(firstLocation!!.latitude, firstLocation!!.longitude)
                meta.putExtra("calDirStart", calDist)
                meta.putExtra("paceStart", GPSTools.getPace(totalTime, calDist))
            }

            if (lastCp != null && lastLocation != null){
                meta.putExtra("dirDirCp",
                    GPSTools.getDirectDistance(
                        lastCp!!.pLat,
                        lastCp!!.pLng,
                        lastLocation!!.latitude,
                        lastLocation!!.longitude))

                val calDist = calculateDistance(lastCp!!.pLat, lastCp!!.pLng)

                meta.putExtra("calDirCp", calDist)
                meta.putExtra("paceCp", GPSTools.getPace(totalTime, calDist))

            }

            if (currentSession.isWayPointSet && lastLocation != null){
                meta.putExtra("dirDirWp",
                    GPSTools.getDirectDistance(
                        currentSession.wLat,
                        currentSession.wLng,
                        lastLocation!!.latitude,
                        lastLocation!!.longitude))

                val calDist = calculateDistance(currentSession.wLat, currentSession.wLng)

                meta.putExtra("calDirWp", calDist)
                meta.putExtra("paceWp", GPSTools.getPace(totalTime, calDist))
            }

            LocalBroadcastManager.getInstance(applicationContext)
                .sendBroadcast(meta)

            updateNotification()

        }, 0, 1, TimeUnit.SECONDS)
    }

    private fun stopMetaUpdating(){
        try {
            thread.shutdown()
        }catch (ignored: Exception){}

        NotificationManagerCompat.from(this).cancel(0)
    }

    private fun updateNotification(){
        val notifyView = RemoteViews(packageName, R.layout.notification_ui)

        val intentCp = Intent(Intents.INTENT_ADD_CP.getAction())
        val intentWp = Intent(Intents.INTENT_ADD_WP.getAction())
        val intent = Intent(this, UI::class.java)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val pendingIntentCp = PendingIntent.getBroadcast(this, 0, intentCp, 0)
        val pendingIntentWp = PendingIntent.getBroadcast(this, 0, intentWp, 0)

        notifyView.setOnClickPendingIntent(R.id.notBCp, pendingIntentCp)
        notifyView.setOnClickPendingIntent(R.id.notBWp, pendingIntentWp)

        val totalTime = GPSTools.getTimeBetween(Timestamp(Date().time), currentSession.creatingTime)

        notifyView.setTextViewText(R.id.notTT, GPSTools.formatRawTime(totalTime))

        if (firstLocation != null && lastLocation != null){
            notifyView.setTextViewText(R.id.notSD, "${GPSTools.getDirectDistance(
                firstLocation!!.latitude,
                firstLocation!!.longitude,
                lastLocation!!.latitude,
                lastLocation!!.longitude)}m")

            val calDist = calculateDistance(firstLocation!!.latitude, firstLocation!!.longitude)
            notifyView.setTextViewText(R.id.notSC, "${calDist}m")
            notifyView.setTextViewText(R.id.notSP, GPSTools.getPace(totalTime, calDist))
        }

        if (lastCp != null && lastLocation != null){
            notifyView.setTextViewText(R.id.notCD, "${GPSTools.getDirectDistance(
                lastCp!!.pLat,
                lastCp!!.pLng,
                lastLocation!!.latitude,
                lastLocation!!.longitude)}m")

            val calDist = calculateDistance(lastCp!!.pLat, lastCp!!.pLng)
            notifyView.setTextViewText(R.id.notCC, "${calDist}m")
            notifyView.setTextViewText(R.id.notCP, GPSTools.getPace(totalTime, calDist))
        }

        if (currentSession.isWayPointSet && lastLocation != null){
            notifyView.setTextViewText(R.id.notWD, "${GPSTools.getDirectDistance(
                currentSession.wLat,
                currentSession.wLng,
                lastLocation!!.latitude,
                lastLocation!!.longitude)}m")

            val calDist = calculateDistance(currentSession.wLat, currentSession.wLng)
            notifyView.setTextViewText(R.id.notWC, "${calDist}m")
            notifyView.setTextViewText(R.id.notWP, GPSTools.getPace(totalTime, calDist))
        }

        var builder = NotificationCompat.Builder(this, "com.tslex.lifetrack.notify")
            .setSmallIcon(R.drawable.marker)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        builder.setContent(notifyView)

        NotificationManagerCompat.from(this).notify(0, builder.build())
    }

    override fun onLocationChanged(location: Location?) {
        addRp(location!!)
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
//                    val latitude = intent.getDoubleExtra("lat", .0)
//                    val longitude = intent.getDoubleExtra("lng", .0)
//                    addWp(latitude, longitude)
                    addWp()
                }
                Intents.INTENT_ADD_CP.getAction() -> {
                    addCp()
                }
                else -> return
            }
        }
    }
}
