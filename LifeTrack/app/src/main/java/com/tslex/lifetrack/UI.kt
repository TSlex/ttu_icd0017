package com.tslex.lifetrack

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class UI : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var myMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationProvider: String
    private lateinit var criteria: Criteria
    private lateinit var preferences: SharedPreferences
    private lateinit var thread: ScheduledExecutorService

    private val broadcastReceiver = UIBroadcastReceiver()
    private val TAG = this::class.java.simpleName

    private var isMapReady = false
    private var state: UIState = UIState.NONE

    private var lastWp: Marker? = null
    private var lastCp: Marker? = null

    private var polyline: Polyline? = null

    private var isRequestingZoom = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        //hide
        buttonPause.visibility = View.GONE
        buttonResume.visibility = View.GONE
        buttonCommit.visibility = View.GONE
        buttonCancel.visibility = View.GONE
        aim.visibility = View.GONE

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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Intentfilters
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intents.INTENT_UI_UPDATE_LOCATION.getAction())
        intentFilter.addAction(Intents.INTENT_UI_UPDATE_META.getAction())
        intentFilter.addAction(Intents.INTENT_UI_PLACE_CP.getAction())
        intentFilter.addAction(Intents.INTENT_UI_PLACE_WP.getAction())
        intentFilter.addAction(Intents.INTENT_COMPASS_UPDATE.getAction())

        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(broadcastReceiver, intentFilter)

        criteria = Criteria()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProvider = locationManager.getBestProvider(criteria, false)!!
        Log.d("MAIN", locationProvider)

        thread = Executors.newScheduledThreadPool(1)
        thread.scheduleAtFixedRate({
            LocalBroadcastManager.getInstance(applicationContext)
                .sendBroadcast(Intent(Intents.INTENT_COMPASS_UPDATE.getAction()))
        }, 0, 100, TimeUnit.MILLISECONDS)

        applySettings()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        thread.shutdown()
        super.onDestroy()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        applySettings()
        if (isMapReady) {
            updateMap()
        }
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }

    fun onZoomButtonClicked(view: View) {

        isRequestingZoom = true

        try {
            locationManager.requestLocationUpdates(2000, 0.1f, criteria, this, null)
        } catch (e: SecurityException) {
            Log.e("MAIN", e.toString())
        }
    }

//    fun onCommitButtonClicked(view: View) {
//
//        when (state) {
//            UIState.ADDING_WAYPOINT -> {
////                LocalBroadcastManager.getInstance(applicationContext)
////                    .sendBroadcast(Intent(Intents.INTENT_ADD_WP.getAction()))
//
//                val intent = Intent(Intents.INTENT_ADD_WP.getAction())
//                val tmp = myMap.cameraPosition
//                intent.putExtra("lat", tmp.target.latitude)
//                intent.putExtra("lng", tmp.target.longitude)
//                placeWp(tmp.target.latitude, tmp.target.longitude)
//
//                LocalBroadcastManager.getInstance(applicationContext)
//                    .sendBroadcast(intent)
//            }
//            UIState.ADDING_CHECKPOINT -> {
//                LocalBroadcastManager.getInstance(applicationContext)
//                    .sendBroadcast(Intent(Intents.INTENT_ADD_CP.getAction()))
//            }
//        }
//
//        state = UIState.NONE
//
//        buttonCp.visibility = View.VISIBLE
//        buttonWp.visibility = View.VISIBLE
//        buttonCommit.visibility = View.GONE
//        buttonCancel.visibility = View.GONE
//        aim.visibility = View.GONE
//    }
//
//    fun onCancelButtonClicked(view: View) {
//        state = UIState.NONE
//
//        buttonCp.visibility = View.VISIBLE
//        buttonWp.visibility = View.VISIBLE
//        buttonCommit.visibility = View.GONE
//        buttonCancel.visibility = View.GONE
//        aim.visibility = View.GONE
//    }

    fun onCpButtonClicked(view: View) {
//        state = UIState.ADDING_CHECKPOINT
//
//        buttonCp.visibility = View.GONE
//        buttonWp.visibility = View.GONE
//        buttonCommit.visibility = View.VISIBLE
//        buttonCancel.visibility = View.VISIBLE
//        aim.visibility = View.VISIBLE

        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(Intents.INTENT_ADD_CP.getAction()))
    }

    fun onWpButtonClicked(view: View) {
//        state = UIState.ADDING_WAYPOINT
//
//        buttonCp.visibility = View.GONE
//        buttonWp.visibility = View.GONE
//        buttonCommit.visibility = View.VISIBLE
//        buttonCancel.visibility = View.VISIBLE
//        aim.visibility = View.VISIBLE

        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(Intents.INTENT_ADD_WP.getAction()))
    }

    fun onStartButtonClicked(view: View) {
        val service = Intent(this, GPSService::class.java)
        startService(service)

        myMap.clear()
        buttonStart.visibility = View.GONE
        buttonPause.visibility = View.VISIBLE

        setPointsButtonsEnabled(true)

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
                .setPositiveButton("Stop") { _, _ -> stopServise() }
                .setNegativeButton("Cancel"){_, _ -> {}}
            confirm.create().show()
        }
    }

    private fun stopServise(){
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(Intents.INTENT_TRACKING_STOP.getAction()))

        buttonPause.visibility = View.GONE
        buttonResume.visibility = View.GONE
        buttonStart.visibility = View.VISIBLE

        setPointsButtonsEnabled(false)
    }

    fun onMenuButtonClicked(view: View) {
        val intent = Intent(this, Menu::class.java)
        startActivity(intent)
    }

    fun onTestButtonClicked(view: View) {
        Log.d(TAG, "${preferences.getBoolean("north_face", true)}")
        Log.d(TAG, "${preferences.getBoolean("center_map", true)}")
        Log.d(TAG, "${preferences.getBoolean("compass", true)}")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap

        //now it is time to update map
        isMapReady = true
        updateMap()

        //now we can unlock buttons
        setControlsButtonsEnabled(true)
    }

    fun updateMap() {
        myMap.isMyLocationEnabled = false
        myMap.uiSettings.isCompassEnabled = false
//        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

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

    fun setControlsButtonsEnabled(bool: Boolean) {
        buttonStart.isEnabled = bool
        buttonPause.isEnabled = bool
        buttonResume.isEnabled = bool
        buttonStop.isEnabled = bool
    }

    fun setPointsButtonsEnabled(bool: Boolean) {
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
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
//        myMap.addMarker(MarkerOptions().position(latLng).title("you kill me every time..."))
        myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
//        polyline!!.points.add(latLng)
        val tmp = polyline!!.points
        tmp.add(latLng)
        polyline!!.points = tmp
    }

    fun placeCp(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        lastCp = myMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("cp_marker", 150, 150)))
        )
        myMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    fun placeWp(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        lastWp?.remove()
        lastWp = myMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("wp_marker", 150, 150)))
        )
        myMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    fun applySettings() {
        compass.isVisible = preferences.getBoolean(getString(R.string.pref_compass), true)
    }

    fun resizeBitmap(drawableName: String?, width: Int, height: Int): Bitmap? {
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
                    compass.rotation = -1 * myMap.cameraPosition.bearing
                }
                else -> return
            }
        }
    }
}
