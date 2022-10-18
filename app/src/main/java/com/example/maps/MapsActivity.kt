package com.example.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.maps.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
    GoogleMap.OnInfoWindowCloseListener {


    private lateinit var locationRequest: LocationRequest

    private lateinit var binding: ActivityMapsBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location
    private val permissionCode = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createLocationRequest()

        Click_me.setOnClickListener {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            getCurrentUserLocation()
        }
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val currentLocation = locationResult.lastLocation
            Log.d("Locations", currentLocation!!.latitude.toString() + "," + currentLocation!!.longitude)
        }
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentUserLocation() {

        //checking user granted the permission of location or not
        //if the user granted the permission so only we are able to get the current location
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            //if permission is not granted then we request the permission
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionCode
            )
            return
        }
        //last location provide us the most recently available location of the user
        //this listener we use it when our task completed successfully

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->

            //check location is null or not because there may be some cases where location may be null for example location is turn off in your device setting
            if (location != null) {
                currentLocation = location
                Toast.makeText(
                    applicationContext, currentLocation.latitude.toString() + " and " +
                            currentLocation.longitude.toString(), Toast.LENGTH_SHORT
                ).show()

                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)

            } else {
                val lm = getSystemService(LOCATION_SERVICE) as LocationManager
                if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                ) {
                    // Build the alert dialog
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setTitle("Location Services Not Active")
                    builder.setMessage("Please enable Location Services and GPS")
                    builder.setNegativeButton("Cancel"){ _, _ ->

                    }
                    builder.setPositiveButton(
                        "OK"
                    ) { _, _ -> // Show location settings when the user acknowledges the alert dialog
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    }
                    val alertDialog: Dialog = builder.create()
                    alertDialog.setCanceledOnTouchOutside(false)
                    alertDialog.show()
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest!!.interval = 1000
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    //overwrite method to request the permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCode ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentUserLocation()
                }

        }

    }


    //this on map ready function is triggered when the map is ready to be used and provide a non null instance of google map
    override fun onMapReady(googleMap: GoogleMap) {

        googleMap.clear()

        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions =
            MarkerOptions().position(latLng).title("Current Location").snippet("I'm here ")
                .draggable(false).alpha(0.6f)

        googleMap.addMarker(markerOptions)!!
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f))

        googleMap.setOnInfoWindowClickListener(this)
        googleMap.setOnInfoWindowCloseListener(this)

    }

    override fun onInfoWindowClick(p0: Marker) {
        Toast.makeText(this, "Alpha value = ${p0.alpha} and draggable : ${p0.isDraggable}", Toast.LENGTH_SHORT).show()
    }

    override fun onInfoWindowClose(p0: Marker) {
        Toast.makeText(this, "Info Window Closed", Toast.LENGTH_SHORT).show()
    }

}