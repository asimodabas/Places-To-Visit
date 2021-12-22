package com.asimodabas.places_to_visit.view

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.asimodabas.places_to_visit.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.asimodabas.places_to_visit.databinding.ActivityMapsBinding
import com.asimodabas.places_to_visit.model.Place
import com.asimodabas.places_to_visit.roomdb.PlaceDao
import com.asimodabas.places_to_visit.roomdb.PlaceDatabase
import com.google.android.material.snackbar.Snackbar

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var permissionLAuncher: ActivityResultLauncher<String>
    private var trackBoolean: Boolean? = null
    private var selectedLatitute: Double? = null
    private var selectedLongitute: Double? = null
    private lateinit var db: PlaceDatabase
    private lateinit var placeDao: PlaceDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()
        sharedPreferences =
            this.getSharedPreferences("com.asimodabas.places_to_visit", MODE_PRIVATE)
        trackBoolean = false

        selectedLatitute = 0.0
        selectedLongitute = 0.0

        db = Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places").build()
        placeDao = db.placeDao()

        binding.saveButton.setOnClickListener {
            save()
        }
        binding.deleteButton.setOnClickListener {
            delete()
        }
    }

    /*
    Markantalya 36.89294589006185, 30.70433979916466

    // Add a marker in Markantalya and move the camera
    val martAntalya = LatLng(36.89294589006185, 30.70433979916466)

    mMap.addMarker(MarkerOptions().position(martAntalya).title("Markantalya"))
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(martAntalya,16f))
    */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                trackBoolean = sharedPreferences.getBoolean("trackBoolean", false)
                if (trackBoolean == false) {
                    //println("location"+location.toString())
                    val userLocation = LatLng(location.altitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16f))
                    sharedPreferences.edit().putBoolean("trackBoolean", true).apply()
                }
            }
        }

        // permission will be taken

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

                Snackbar.make(
                    binding.root,
                    "Permission needed for location",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Give permission") {
                    //Request permission
                    permissionLAuncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            } else {
                //Request permission
                permissionLAuncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else {
            //Permission granted
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastLocation != null) {
                val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 16f))
            }
            mMap.isMyLocationEnabled = true
        }
    }

    private fun registerLauncher() {
        permissionLAuncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        //Permission granted
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0f,
                            locationListener
                        )
                        val lastLocation =
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (lastLocation != null) {
                            val lastUserLocation =
                                LatLng(lastLocation.latitude, lastLocation.longitude)
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    lastUserLocation,
                                    16f
                                )
                            )
                        }
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    //Permission denied
                    Toast.makeText(this@MapsActivity, "Permission needed", Toast.LENGTH_LONG).show()

                }
            }
    }

    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0))

        selectedLatitute = p0.latitude
        selectedLongitute = p0.latitude
    }

    fun save() {

        if (selectedLatitute != null && selectedLongitute != null) {
            val place = Place(binding.placeText.text.toString(), selectedLatitute!!, selectedLongitute!!)
            placeDao.insert(place)
        }
    }

    fun delete() {

    }


}