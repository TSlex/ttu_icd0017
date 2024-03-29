package com.tslex.gmaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.GoogleApi

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {


    private lateinit var mMap: GoogleMap
    lateinit var locationManager: LocationManager
    lateinit var locationProvider: String

    private val MIN_TIME: Long = 1
    private val MIN_DIST: Float = 1f

    override fun onCreate(savedInstanceState: Bundle?) {

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 999
            )
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //location
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        var enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//        if (!enabled) {
//            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
//        }

//        var criteria = Criteria()

//        locationProvider = locationManager.getBestProvider(criteria, false)!!


//        var location = locationManager.getLastKnownLocation(locationProvider)

//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.5F, this)
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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

//        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, this)
        }catch (ex : SecurityException){
            ex.printStackTrace()
        }
    }

//    @SuppressLint("MissingPermission")
//    override fun onResume() {
//        super.onResume()
//
//        if (locationManager != null) {
//            locationManager.requestLocationUpdates(locationProvider, 1000, 0.5f, this)
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//
//        if (locationManager != null) {
//            locationManager.removeUpdates(this)
//        }
//    }

    override fun onLocationChanged(location: Location?) {
//        val locationLatLng = LatLng(location!!.latitude, location!!.longitude)
//        val location = locationManager.getLastKnownLocation(locationProvider)
        val locationLatLng = LatLng(location!!.latitude, location!!.longitude)
        mMap.addMarker(MarkerOptions().position(locationLatLng).title("please work!"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(locationLatLng))
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
