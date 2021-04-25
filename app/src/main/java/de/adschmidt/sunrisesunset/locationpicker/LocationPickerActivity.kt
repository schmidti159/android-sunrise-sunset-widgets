package de.adschmidt.sunrisesunset.locationpicker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.adschmidt.sunrisesunset.R
import de.adschmidt.sunrisesunset.TAG
import de.adschmidt.sunrisesunset.locationpicker.LocationPicker.Companion.PARAMS_KEY
import de.adschmidt.sunrisesunset.locationpicker.LocationPicker.Companion.RESULT_KEY
import de.adschmidt.sunrisesunset.locationpicker.PermissionUtils.isPermissionGranted
import de.adschmidt.sunrisesunset.locationpicker.PermissionUtils.requestPermission

class LocationPickerActivity() : AppCompatActivity(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private lateinit var location: LatLng
    private lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_picker)

        geocoder = Geocoder(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.location_picker_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val pickLocationButton = findViewById<Button>(R.id.location_picker_pick_location)
        pickLocationButton.setOnClickListener {
            setActivityResult()
            finish()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val params = intent.getParcelableExtra<LocationPicker.Params>(PARAMS_KEY)
        location = LatLng(params.latitude, params.longitude)
        val marker = map.addMarker( MarkerOptions().position(location).title("Location"))
        map.moveCamera(CameraUpdateFactory.newLatLng(marker.position))
        map.setOnMapLongClickListener {
            newPosition ->
            run {
                location = newPosition
                marker.position = newPosition
            }
        }
        enableCurrentLocation()
    }

    private fun enableCurrentLocation() {
        if (!::map.isInitialized) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "access to fine location was already granted before")
            map.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            Log.i(TAG, "requesting access to fine location")
            requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.i(TAG, "access to fine location has been granted.")
            enableCurrentLocation()
        }
    }

    override fun onBackPressed() {
        setActivityResult()
        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        setActivityResult()
        finish()
        return true
    }

    private fun setActivityResult() {
        val data = Intent()
        val address = getCurrentAddressString()
        data.putExtra(RESULT_KEY, LocationPicker.Result(location.latitude, location.longitude, 0.0, address))
        Log.d(TAG, "Returning picked location data: $location, $address")
        setResult(RESULT_OK, data)
    }

    private fun getCurrentAddressString(): String {
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if(addresses.isEmpty()) {
            return "[${location.latitude}, ${location.longitude}]"
        }
        val addr = addresses[0]
        var addressLines = ""
        Log.d(TAG, "addr: $addr")
        Log.d(TAG, "maxAddressLinesIndex: ${addr.maxAddressLineIndex}")
        for(i in 0 .. addr.maxAddressLineIndex) {
            Log.d(TAG, "line $i: ${addr.getAddressLine(i)}")
            if(i > 0) {
                addressLines += ", "
            }
            addressLines += addr.getAddressLine(i)
        }
        Log.d(TAG, "setting current address to $addressLines")
        return addressLines
    }
}