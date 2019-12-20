package com.tslex.lifetrack

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.tslex.lifetrack.domain.Session
import com.tslex.lifetrack.repo.PointRepo
import com.tslex.lifetrack.repo.SessionRepo
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ScheduledExecutorService


class UI : AppCompatActivity(), OnMapReadyCallback, LocationListener, SensorEventListener {

    private lateinit var myMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationProvider: String
    private lateinit var criteria: Criteria
    private lateinit var preferences: SharedPreferences

    lateinit var sensorManager: SensorManager
    lateinit var accSensor: Sensor

    private val broadcastReceiver = UIBroadcastReceiver()
    private val intentFilter = IntentFilter()

    private val TAG = this::class.java.simpleName

    private var isMapReady = false

    private var lastWp: Marker? = null
    private var lastCp: Marker? = null
    private var currentPositionMarker: Marker? = null

    private var polyline: Polyline? = null

    private var isSessionStarted = false
    private var isRequestingZoom = false
    private var isRequestingRestore = false
    private var isNotEmpty = false

    private var currentSession: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("LIFE", "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        //hide
        buttonPause.visibility =        View.GONE
        buttonResume.visibility =       View.GONE
        locationUpdating.visibility =   View.GONE

        //lock buttons
        setControlsButtonsEnabled(false)
        setPointsButtonsEnabled(false)

        //request for permissions
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
//            Manifest.permission.INTERNET
            ), PackageManager.PERMISSION_GRANTED
        )

        //Sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Intentfilters
        intentFilter.addAction(Intents.INTENT_UI_UPDATE_LOCATION.getAction())
        intentFilter.addAction(Intents.INTENT_UI_UPDATE_META.getAction())
        intentFilter.addAction(Intents.INTENT_UI_PLACE_CP.getAction())
        intentFilter.addAction(Intents.INTENT_UI_PLACE_WP.getAction())
        intentFilter.addAction(Intents.INTENT_COMPASS_UPDATE.getAction())

        criteria = Criteria()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProvider = locationManager.getBestProvider(criteria, false)!!
        Log.d("MAIN", locationProvider)

        applySettings()
    }

    override fun onDestroy() {
        Log.d("LIFE", "onDestroy")
        super.onDestroy()
    }

    override fun onResume() {
        Log.d("LIFE", "onResume")
        applySettings()

        if (isMapReady) {
            updateMap()
        }

        if (isSessionStarted){
            buttonStart.visibility = View.GONE
            buttonPause.visibility = View.VISIBLE

            setPointsButtonsEnabled(true)
        }

        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_FASTEST)
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(broadcastReceiver, intentFilter)

        super.onResume()
    }

    override fun onPause() {
        Log.d("LIFE", "onPause")

        sensorManager.unregisterListener(this)
        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(broadcastReceiver)

        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val values = event!!.values
        val degree = values[0]

        compass.rotation = degree * -1

        val isMapFaceNorth = preferences.getBoolean(getString(R.string.pref_north_face), false)
        if (isMapFaceNorth && isMapReady){
            faceCamera(degree * -1)
        }
    }

    private fun faceCamera(degree: Float){
        val currentCamPosition = myMap.cameraPosition.target
        val currentPlace = CameraPosition.Builder()
                    .target(LatLng(currentCamPosition.latitude, currentCamPosition.longitude))
                    .bearing(degree).build()
        myMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    fun onZoomButtonClicked(view: View) {

        if (currentPositionMarker != null){
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPositionMarker!!.position, 16f))
            locationManager.removeUpdates(this)
        }

        isRequestingZoom = true
        locationUpdating.visibility = View.VISIBLE

        try {
            locationManager.requestLocationUpdates(2000, 0.1f, criteria, this, null)
        } catch (e: SecurityException) {
            Log.e("MAIN", e.toString())
        }
    }

    fun onCpButtonClicked(view: View) {
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(Intents.INTENT_ADD_CP.getAction()))
    }

    fun onWpButtonClicked(view: View) {
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(Intents.INTENT_ADD_WP.getAction()))
    }

    fun onStartButtonClicked(view: View) {
        val service = Intent(this, GPSService::class.java)
        startService(service)

        isSessionStarted = true

        locationUpdating.visibility = View.VISIBLE

        isNotEmpty = false

        myMap.clear()
        buttonStart.visibility = View.GONE
        buttonPause.visibility = View.VISIBLE

        setPointsButtonsEnabled(true)

        addEmptyPolyline()

        isNotEmpty = true
    }

    private fun addEmptyPolyline(){
        polyline = myMap.addPolyline(
            PolylineOptions()
                .startCap(RoundCap())
                .endCap(RoundCap())
        )

        polyline!!.width = 20f
//        polyline!!.color = titleColor
    }

    fun onPauseButtonClicked(view: View) {
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(Intents.INTENT_TRACKING_PAUSE.getAction()))

        buttonPause.visibility = View.GONE
        buttonResume.visibility = View.VISIBLE
    }

    fun onResumeButtonClicked(view: View) {

        locationUpdating.visibility = View.VISIBLE


        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(Intents.INTENT_TRACKING_RESUME.getAction()))

        buttonResume.visibility = View.GONE
        buttonPause.visibility = View.VISIBLE
    }

    fun onStopButtonClicked(view: View) {
        if (buttonStart.visibility == View.GONE) {
            val confirm = AlertDialog.Builder(this)
            confirm.setTitle("Stop listener?")
                .setMessage("All session data will be saved")
                .setPositiveButton("Stop") { _, _ -> stopService() }
                .setNegativeButton("Cancel"){_, _ -> {}}
            confirm.create().show()
        }
    }

    private fun stopService(){
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(Intents.INTENT_TRACKING_STOP.getAction()))

        buttonPause.visibility = View.GONE
        buttonResume.visibility = View.GONE
        buttonStart.visibility = View.VISIBLE

        setPointsButtonsEnabled(false)
        isSessionStarted = false
    }

    fun onSettingsButtonClicked(view: View) {
        val intent = Intent(this, Preferences::class.java)
        startActivity(intent)
    }

    fun onSessionsButtonClicked(view: View) {
        val sessions = Intent(this, SessionListUi::class.java)
        startActivityForResult(sessions, 999)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d("LIFE", "onRestore")
        super.onRestoreInstanceState(savedInstanceState)
        isNotEmpty = savedInstanceState.getBoolean("notEmpty")
        isSessionStarted = savedInstanceState.getBoolean("sessionStarted")
        isRequestingRestore = true

        totalTime.text =    savedInstanceState.getString("totalTime") ?: "00:00:00"
        paceStart.text =    savedInstanceState.getString("paceStart") ?: "00:00:00"
        paceCp.text =       savedInstanceState.getString("paceCp") ?: "00:00:00"
        paceWp.text =       savedInstanceState.getString("paceWp") ?: "00:00:00"

        dirDirStart.text =  "${savedInstanceState.getInt("dirDirStart")}m" ?: "0m"
        dirDirCp.text =     "${savedInstanceState.getInt("dirDirCp")}m" ?: "0m"
        dirDirWp.text =     "${savedInstanceState.getInt("dirDirWp")}m" ?: "0m"

        calDirStart.text =  "${savedInstanceState.getInt("calDirStart")}m" ?: "0m"
        calDirCp.text =     "${savedInstanceState.getInt("calDirCp")}m" ?: "0m"
        calDirWp.text =     "${savedInstanceState.getInt("calDirWp")}m" ?: "0m"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("LIFE", "onSave")
        super.onSaveInstanceState(outState)
        outState.putBoolean("notEmpty", isNotEmpty)
        outState.putBoolean("sessionStarted", isSessionStarted)

        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(broadcastReceiver)

        val sessions = SessionRepo(this).open()
        if (currentSession == null){
            currentSession = sessions.getLast()
        }
        sessions.close()

        if (currentSession == null) return

        outState.putString("totalTime", currentSession!!.sessionTime)
        outState.putString("paceStart", currentSession!!.paceStart)
        outState.putString("paceCp", currentSession!!.paceCp)
        outState.putString("paceWp", currentSession!!.paceWp)

        outState.putInt("dirDirStart", currentSession!!.dirDistStart)
        outState.putInt("dirDirCp", currentSession!!.dirDirCp)
        outState.putInt("dirDirWp", currentSession!!.dirDirWp)
        outState.putInt("calDirStart", currentSession!!.calDirStart)
        outState.putInt("calDirCp", currentSession!!.calDirCp)
        outState.putInt("calDirWp", currentSession!!.calDirWp)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("ActivityResult", resultCode.toString())
        when (resultCode){
            13 -> {
                stopService()
                myMap.clear()

                val sessionId = data?.getIntExtra("sessionId", 0) ?: 0
                if (sessionId == 0) return

                restoreMap(sessionId)
            }
        }
    }

    private fun restoreMap(sessionId: Int){

        val sessions = SessionRepo(this).open()
        var session: Session?

        if (sessionId == 0) {
            session = sessions.getLast()
            sessions.close()
        }
        else{
            session = sessions.getById(sessionId)
            sessions.close()
        }

        if (session == null) return

//                Log.d("ActivityResult", "got session")

        val points = PointRepo(this).open()
        val pointList = points.getAll(session.id)
        points.close()

        val rPoints = pointList.filter {point -> point.typeId != 1}
        val cPoints = pointList.filter {point -> point.typeId == 1}

//                Log.d("ActivityResult", "got points")

        addEmptyPolyline()

        val tmp = polyline!!.points

        for (point in rPoints){
            tmp.add(LatLng(point.pLat, point.pLng))
        }

        val last = rPoints.last()

        polyline!!.points = tmp

        for (point in cPoints){
            lastCp = myMap.addMarker(
                MarkerOptions()
                    .position(LatLng(point.pLat, point.pLng))
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("cp_marker", 150, 150)))
            )
        }

        if (session.isWayPointSet){
            lastWp = myMap.addMarker(
                MarkerOptions()
                    .position(LatLng(session.wLat, session.wLng))
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("wp_marker", 150, 150)))
            )
        }

        currentPositionMarker = myMap.addMarker(
            MarkerOptions()
                .position(LatLng(last.pLat, last.pLng))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("curr_pos_marker", 100, 100)))
                .anchor(0.5f, 0.5f)
        )

        if (sessionId != 0) {

            totalTime.text =    session.sessionTime
            paceStart.text =    session.paceStart
            paceCp.text =       session.paceCp
            paceWp.text =       session.paceWp

            dirDirStart.text =  "${session.dirDistStart}m"
            dirDirCp.text =     "${session.dirDirCp}m"
            dirDirWp.text =     "${session.dirDirWp}m"

            calDirStart.text =  "${session.calDirStart}m"
            calDirCp.text =     "${session.calDirCp}m"
            calDirWp.text =     "${session.calDirWp}m"

            myMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(last.pLat, last.pLng),
                    16f
                )
            )
        }

        currentSession = session
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap

        //now it is time to update map
        isMapReady = true
        updateMap()

        //now we can unlock buttons
        setControlsButtonsEnabled(true)

        if (isRequestingRestore && isNotEmpty){
            restoreMap(0)
            isRequestingRestore = false
        }
    }

    private fun updateMap() {
        myMap.isMyLocationEnabled = false
        myMap.uiSettings.isCompassEnabled = false
        faceCamera(0f)

        val mapStyles: Array<String> = resources.getStringArray(R.array.pref_map_style_values)
        when (preferences.getString(getString(R.string.pref_map_style), mapStyles[0])) {
            mapStyles[0] -> myMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.light_map_style
                )
            )
            mapStyles[1] -> myMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.dark_map_style
                )
            )
        }
    }

    private fun setControlsButtonsEnabled(bool: Boolean) {
        buttonStart.isEnabled = bool
        buttonPause.isEnabled = bool
        buttonResume.isEnabled = bool
        buttonStop.isEnabled = bool
    }

    private fun setPointsButtonsEnabled(bool: Boolean) {
        buttonWp.isEnabled = bool
        buttonCp.isEnabled = bool
    }

    override fun onLocationChanged(location: Location?) {
        val latLng = LatLng(location!!.latitude, location.longitude)
//        myMap.addMarker(MarkerOptions().position(latLng).title("kill me please"))
//        myMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))

        if (isRequestingZoom) {
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            locationManager.removeUpdates(this)
        }

        isRequestingZoom = false
        locationUpdating.visibility = View.GONE
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    fun updateLocation(latitude: Double, longitude: Double) {

        if (!isMapReady) return

        locationUpdating.visibility = View.GONE

        val latLng = LatLng(latitude, longitude)

        val tmp = polyline?.points ?: return
        tmp.add(latLng)
        polyline!!.points = tmp

        val keepCenter = preferences.getBoolean(getString(R.string.pref_center_map), true)
        if (keepCenter) {
            myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }

        currentPositionMarker?.remove()
        currentPositionMarker = myMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("curr_pos_marker", 100, 100)))
                .anchor(0.5f, 0.5f)
        )
    }

    fun placeCp(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        lastCp = myMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("cp_marker", 150, 150)))
                .anchor(0.3f, 1f)
        )

        val keepCenter = preferences.getBoolean(getString(R.string.pref_center_map), true)
        if (keepCenter) {
            myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    fun placeWp(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        lastWp?.remove()
        lastWp = myMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("wp_marker", 150, 150)))
                .anchor(0.5f, 0.8f)
        )

        val keepCenter = preferences.getBoolean(getString(R.string.pref_center_map), true)
        if (keepCenter) {
            myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    private fun applySettings() {
        compass.isVisible = preferences.getBoolean(getString(R.string.pref_compass), true)
    }

    private fun resizeBitmap(drawableName: String?, width: Int, height: Int): Bitmap? {
        val imageBitmap = BitmapFactory.decodeResource(
            resources,
            resources.getIdentifier(drawableName, "drawable", packageName)
        )
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    }

    private inner class UIBroadcastReceiver : BroadcastReceiver() {

        private val TAG = this::class.java.simpleName

        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
//            Log.d(TAG, "onReceive")

            val action = intent?.action

            when (action) {
                Intents.INTENT_UI_UPDATE_LOCATION.getAction() -> {
                    val latitude = intent.getDoubleExtra("lat", .0)
                    val longitude = intent.getDoubleExtra("lng", .0)
                    updateLocation(latitude, longitude)
                }
                Intents.INTENT_UI_UPDATE_META.getAction() -> {

                    totalTime.text = intent.getStringExtra("totalTime")

                    dirDirStart.text = "${intent.getIntExtra("dirDirStart", 0)}m"
                    dirDirCp.text = "${intent.getIntExtra("dirDirCp", 0)}m"
                    dirDirWp.text = "${intent.getIntExtra("dirDirWp", 0)}m"

                    calDirStart.text = "${intent.getIntExtra("calDirStart", 0)}m"
                    calDirCp.text = "${intent.getIntExtra("calDirCp", 0)}m"
                    calDirWp.text = "${intent.getIntExtra("calDirWp", 0)}m"

                    paceStart.text = intent.getStringExtra("paceStart")
                    if (paceStart.text == "") paceStart.text = "00:00:00"

                    paceCp.text = intent.getStringExtra("paceCp")
                    if (paceCp.text == "") paceCp.text = "00:00:00"

                    paceWp.text = intent.getStringExtra("paceWp")
                    if (paceWp.text == "") paceWp.text = "00:00:00"

                }
                Intents.INTENT_UI_PLACE_WP.getAction() -> {
                    val latitude = intent.getDoubleExtra("lat", .0)
                    val longitude = intent.getDoubleExtra("lng", .0)
                    placeWp(latitude, longitude)
                }
                Intents.INTENT_UI_PLACE_CP.getAction() -> {
                    val latitude = intent.getDoubleExtra("lat", .0)
                    val longitude = intent.getDoubleExtra("lng", .0)
                    placeCp(latitude, longitude)
                }
                Intents.INTENT_COMPASS_UPDATE.getAction() -> {
//                    compass.rotation = -1 * myMap.cameraPosition.bearing
//                    updateCompass()
                }
                else -> return
            }
        }
    }
}
