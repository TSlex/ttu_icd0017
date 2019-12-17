package com.tslex.lifetrack

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
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

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        //hide
        buttonPause.visibility = View.GONE
        buttonResume.visibility = View.GONE

        //lock buttons
        setControlsButtonsEnabled(false)

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
        intentFilter.addAction(Intents.INTENT_UI_UPDATE.getAction())
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

        updateUI()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        thread.shutdown()
        super.onDestroy()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        updateUI()
        if (isMapReady) {
            updateMap()
        }
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }


    fun onStartButtonClicked(view: View) {
        val service = Intent(this, GPSService::class.java)
        startService(service)

        buttonStart.visibility = View.GONE
        buttonPause.visibility = View.VISIBLE
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
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(Intents.INTENT_TRACKING_STOP.getAction()))

        buttonPause.visibility = View.GONE
        buttonResume.visibility = View.GONE
        buttonStart.visibility = View.VISIBLE
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

    fun updateMap(){
        myMap.isMyLocationEnabled = false
        myMap.uiSettings.isCompassEnabled = false
//        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        val mapStyles: Array<String> = resources.getStringArray(R.array.pref_map_style_values)
        when(preferences.getString(getString(R.string.pref_map_style), mapStyles[0])){
            mapStyles[0] -> myMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.light_map_style))
            mapStyles[1] -> myMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.dark_map_style))
        }
    }

    fun setControlsButtonsEnabled(bool: Boolean){
        buttonStart.isEnabled = bool
        buttonPause.isEnabled = bool
        buttonResume.isEnabled = bool
        buttonStop.isEnabled = bool
    }

    override fun onLocationChanged(location: Location?) {
        val latLng = LatLng(location!!.latitude, location.longitude)
        myMap.addMarker(MarkerOptions().position(latLng).title("kill me please"))
        myMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        myMap.addMarker(MarkerOptions().position(latLng).title("you kill me every time..."))
        myMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    fun updateUI(){
        compass.isVisible = preferences.getBoolean(getString(R.string.pref_compass), true)
    }

    private inner class UIBroadcastReceiver : BroadcastReceiver() {

        private val TAG = this::class.java.simpleName

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive")

            val action = intent?.action

            when (action) {
                Intents.INTENT_UI_UPDATE.getAction() -> {
                    val latitude = intent.getDoubleExtra("lat", .0)
                    val longitude = intent.getDoubleExtra("lng", .0)
                    updateLocation(latitude, longitude)
                }
                Intents.INTENT_COMPASS_UPDATE.getAction() -> {
                    compass.rotation = -1 * myMap.cameraPosition.bearing
                }
                else -> return
            }
        }
    }
}
