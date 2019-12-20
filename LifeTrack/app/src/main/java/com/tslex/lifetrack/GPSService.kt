package com.tslex.lifetrack

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
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
import java.sql.Timestamp
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
    private var unsureLocation: Location? = null

    private var lastCp: Point? = null

    private lateinit var pointTypes: ArrayList<PType>

    override fun onCreate() {
        super.onCreate()
        Log.d("Servicelife", "onCreate")

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

        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(broadcastReceiver)

        unregisterReceiver(broadcastReceiver)

        stopListener()
        NotificationManagerCompat.from(this).cancel(1)
        Log.d("Servicelife", "onDestroy")
    }

    private fun setUpLocations() {
        criteria = AssistTools.getCriteria()
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
            locationManager.requestLocationUpdates(1000, 0.1f, criteria, this, null)
        } catch (e: SecurityException) {
            Log.e("MAIN", e.toString())
        }

        startForeground(1, getNotification())

        startMetaUpdating()
    }

    private fun stopListener() {
        Log.d(TAG, "onListenerStop")

        locationManager.removeUpdates(this)

        stopMetaUpdating()

        stopForeground(true)
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

        currentSession.isWayPointSet = true
        currentSession.wLat = tmp.latitude
        currentSession.wLng = tmp.longitude
        updateSession()

        addRp(tmp)

        val intent = Intent(Intents.INTENT_UI_PLACE_WP.getAction())
        intent.putExtra("lat", tmp.latitude)
        intent.putExtra("lng", tmp.longitude)

        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(intent)
    }

    private fun updateSession(){
        val sessions = SessionRepo(this).open()
        sessions.update(currentSession)
        sessions.close()
    }

    private fun addRp(location: Location): Point{
        Log.d("rtrack", "added")

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
                    sum += AssistTools.getDirectDistance(lastPoint.pLat, lastPoint.pLng, p.pLat, p.pLng)
                }
                break
            }
            if (lastPoint == null){
                lastPoint = p
                continue
            }
            else{
                sum += AssistTools.getDirectDistance(lastPoint.pLat, lastPoint.pLng, p.pLat, p.pLng)
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
            val totalTime = AssistTools.getTimeBetween(Timestamp(Date().time), currentSession.creatingTime)

            currentSession.sessionTime = AssistTools.formatRawTime(totalTime)
            meta.putExtra("totalTime", currentSession.sessionTime)

            if (firstLocation != null && lastLocation != null){

                currentSession.dirDistStart = AssistTools.getDirectDistance(
                    firstLocation!!.latitude,
                    firstLocation!!.longitude,
                    lastLocation!!.latitude,
                    lastLocation!!.longitude)

                meta.putExtra("dirDirStart", currentSession.dirDistStart)

                val calDist = calculateDistance(firstLocation!!.latitude, firstLocation!!.longitude)

                currentSession.calDirStart = calDist
                currentSession.paceStart = AssistTools.getPace(totalTime, calDist)
                meta.putExtra("calDirStart", currentSession.calDirStart)
                meta.putExtra("paceStart", currentSession.paceStart)
            }

            if (lastCp != null && lastLocation != null){

                currentSession.dirDirCp = AssistTools.getDirectDistance(
                    lastCp!!.pLat,
                    lastCp!!.pLng,
                    lastLocation!!.latitude,
                    lastLocation!!.longitude)
                meta.putExtra("dirDirCp", currentSession.dirDirCp)

                val calDist = calculateDistance(lastCp!!.pLat, lastCp!!.pLng)
                currentSession.calDirCp = calDist
                currentSession.paceCp = AssistTools.getPace(totalTime, calDist)
                meta.putExtra("calDirCp", currentSession.calDirCp)
                meta.putExtra("paceCp", currentSession.paceCp)

            }

            if (currentSession.isWayPointSet && lastLocation != null){
                currentSession.dirDirWp = AssistTools.getDirectDistance(
                    currentSession.wLat,
                    currentSession.wLng,
                    lastLocation!!.latitude,
                    lastLocation!!.longitude)
                meta.putExtra("dirDirWp", currentSession.dirDirWp)

                val calDist = calculateDistance(currentSession.wLat, currentSession.wLng)
                currentSession.calDirWp = calDist
                currentSession.paceWp = AssistTools.getPace(totalTime, calDist)
                meta.putExtra("calDirWp", currentSession.calDirWp)
                meta.putExtra("paceWp", currentSession.paceWp)
            }

            LocalBroadcastManager.getInstance(applicationContext)
                .sendBroadcast(meta)

            updateSession()
            updateNotification()

        }, 0, 1, TimeUnit.SECONDS)
    }

    private fun stopMetaUpdating(){
        try {
            thread.shutdownNow()
        }catch (ignored: Exception){}

        NotificationManagerCompat.from(this).cancel(0)
    }

    private fun updateNotification(){
        NotificationManagerCompat.from(this).notify(1, getNotification())
    }

    private fun getNotification(): Notification{
        val notifyView = RemoteViews(packageName, R.layout.notification_ui)

        val intentCp = Intent(Intents.INTENT_ADD_CP.getAction())
        val intentWp = Intent(Intents.INTENT_ADD_WP.getAction())

        val intent = Intent(this, UI::class.java)
        intent.putExtra("sessionStarted", true)
        intent.putExtra("notEmpty", true)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val pendingIntentCp = PendingIntent.getBroadcast(this, 0, intentCp, 0)
        val pendingIntentWp = PendingIntent.getBroadcast(this, 0, intentWp, 0)

        notifyView.setOnClickPendingIntent(R.id.notBCp, pendingIntentCp)
        notifyView.setOnClickPendingIntent(R.id.notBWp, pendingIntentWp)

        val totalTime = AssistTools.getTimeBetween(Timestamp(Date().time), currentSession.creatingTime)

        notifyView.setTextViewText(R.id.notTT, AssistTools.formatRawTime(totalTime))

        if (firstLocation != null && lastLocation != null){
            notifyView.setTextViewText(R.id.notSD, "${AssistTools.getDirectDistance(
                firstLocation!!.latitude,
                firstLocation!!.longitude,
                lastLocation!!.latitude,
                lastLocation!!.longitude)}m")

            val calDist = calculateDistance(firstLocation!!.latitude, firstLocation!!.longitude)
            notifyView.setTextViewText(R.id.notSC, "${calDist}m")
            notifyView.setTextViewText(R.id.notSP, AssistTools.getPace(totalTime, calDist))
        }

        if (lastCp != null && lastLocation != null){
            notifyView.setTextViewText(R.id.notCD, "${AssistTools.getDirectDistance(
                lastCp!!.pLat,
                lastCp!!.pLng,
                lastLocation!!.latitude,
                lastLocation!!.longitude)}m")

            val calDist = calculateDistance(lastCp!!.pLat, lastCp!!.pLng)
            notifyView.setTextViewText(R.id.notCC, "${calDist}m")
            notifyView.setTextViewText(R.id.notCP, AssistTools.getPace(totalTime, calDist))
        }

        if (currentSession.isWayPointSet && lastLocation != null){
            notifyView.setTextViewText(R.id.notWD, "${AssistTools.getDirectDistance(
                currentSession.wLat,
                currentSession.wLng,
                lastLocation!!.latitude,
                lastLocation!!.longitude)}m")

            val calDist = calculateDistance(currentSession.wLat, currentSession.wLng)
            notifyView.setTextViewText(R.id.notWC, "${calDist}m")
            notifyView.setTextViewText(R.id.notWP, AssistTools.getPace(totalTime, calDist))
        }

        var builder = NotificationCompat.Builder(this, "com.tslex.lifetrack.notify")
            .setSmallIcon(R.drawable.marker)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        builder.setContent(notifyView)

        return builder.build()
    }

    override fun onLocationChanged(location: Location?) {
        Log.d(TAG, "locationUpdate")

        if (lastLocation != null) {
            if (AssistTools.getDirectDistance(
                    location!!.latitude,
                    location!!.longitude,
                    lastLocation!!.latitude,
                    lastLocation!!.longitude
                ) < 2) return

            if (unsureLocation == null
                && AssistTools.getDirectDistance(
                    location!!.latitude,
                    location!!.longitude,
                    lastLocation!!.latitude,
                    lastLocation!!.longitude
                ) > 100
            ) {
                unsureLocation = location
                return
            } else if (unsureLocation != null) {
                if (AssistTools.getDirectDistance(
                        location!!.latitude,
                        location!!.longitude,
                        lastLocation!!.latitude,
                        lastLocation!!.longitude
                    ) <= 100
                ) {
                    addRp(location!!)
                    unsureLocation = null
                    return
                } else {
                    addRp(unsureLocation!!)
                    addRp(location!!)
                    unsureLocation = null
                    return
                }
            }
        }

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
            val action = intent?.action

            Log.d(TAG, action)

            when (action) {
                Intents.INTENT_TRACKING_RESUME.getAction() -> {
                    startListener()
                }
                Intents.INTENT_TRACKING_PAUSE.getAction() -> {
                    stopListener()
                }
                Intents.INTENT_TRACKING_STOP.getAction() -> {
                    stopListener()
                    onDestroy()
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
