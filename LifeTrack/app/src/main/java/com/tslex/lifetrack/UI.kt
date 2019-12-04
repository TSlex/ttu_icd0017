package com.tslex.lifetrack

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import javax.sql.ConnectionEventListener

class UI : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationProvider: String
    private lateinit var criteria: Criteria

    private val broadcastReceiver = UIBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {

        //request for permissions
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
//            Manifest.permission.INTERNET
        ), PackageManager.PERMISSION_GRANTED)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intents.INTENT_UI_UPDATE.getAction())

        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(broadcastReceiver, intentFilter)

        criteria = Criteria()
//        criteria.accuracy = Criteria.ACCURACY_COARSE
//        criteria.powerRequirement = Criteria.POWER_LOW

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationProvider = locationManager.getBestProvider(criteria, false)!!
        Log.d("MAIN", locationProvider)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    fun onStartButtonClicked(view: View){
        val service = Intent(this, GPSService::class.java)
        startService(service)

        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(Intents.INTENT_TRACKING_START.getAction()))
    }
    fun onPauseButtonClicked(view: View){
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(Intents.INTENT_TRACKING_PAUSE.getAction()))
    }
    fun onStopButtonClicked(view: View){
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(Intents.INTENT_TRACKING_STOP.getAction()))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

//        try {
//            locationManager.requestLocationUpdates(locationProvider, 100, 1f, this)
//        }catch (e: SecurityException){
//            Log.e("MAIN", e.toString())
//        }
    }

    override fun onLocationChanged(location: Location?) {
        val latLng = LatLng(location!!.latitude, location.longitude)
        mMap.addMarker(MarkerOptions().position(latLng).title("kill me please"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }
    override fun onProviderEnabled(provider: String?) {
    }
    override fun onProviderDisabled(provider: String?) {
    }

    fun updateLocation(latitude: Double, longitude: Double){
        val latLng = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(latLng).title("you kill me every time..."))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private inner class UIBroadcastReceiver : BroadcastReceiver() {

        private val TAG = this::class.java.simpleName

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive")

            val action = intent?.action

            when(action){
                Intents.INTENT_UI_UPDATE.getAction() -> {
                    val latitude = intent.getDoubleExtra("lat", .0)
                    val longitude = intent.getDoubleExtra("lng", .0)
                    updateLocation(latitude, longitude)
                }
                else -> return
            }
        }
    }
}
