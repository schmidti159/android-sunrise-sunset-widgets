package de.adschmidt.sunrisesunset.preferences

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.location.Address
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import de.adschmidt.sunrisesunset.BuildConfig
import de.adschmidt.sunrisesunset.TAG
import de.adschmidt.sunrisesunset.locationpicker.LocationPickerActivity
import de.adschmidt.sunrisesunset.model.WidgetPreferences.Companion.DEFAULT_LATITUDE
import de.adschmidt.sunrisesunset.model.WidgetPreferences.Companion.DEFAULT_LONGITUDE

class LocationPreference(private val ctx: Context,
                         private val activityLauncher: ActivityResultLauncher<Intent>) : Preference(ctx) {

    var address: String? = null
        private set

    fun handleActivityResult(result: ActivityResult) {
        /*if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val data = result.data!!
            val latitude = data.getDoubleExtra(LATITUDE, 0.0)
            val longitude = data.getDoubleExtra(LONGITUDE, 0.0)
            address = data.getStringExtra(LOCATION_ADDRESS)
            preferenceDataStore?.putFloat("$key.latitude", latitude.toFloat())
            preferenceDataStore?.putFloat("$key.longitude", longitude.toFloat())
            preferenceDataStore?.putString("$key.address", address)
            Log.d(TAG, "Updated location to $address ($latitude, $longitude)")

        }
        if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG, "cancelled location picker")
        }*/
    }

    override fun onClick() {
        super.onClick()
/*
        val latitude = preferenceDataStore?.getFloat("$key.latitude", DEFAULT_LATITUDE.toFloat())
            ?: DEFAULT_LONGITUDE.toFloat()
        val longitude = preferenceDataStore?.getFloat("$key.longitude", DEFAULT_LONGITUDE.toFloat())
            ?: DEFAULT_LATITUDE.toFloat()

        val locationPickerIntent = LocationPickerActivity.Builder()
            .withLocation(latitude.toDouble(), longitude.toDouble())
            .withGeolocApiKey(BuildConfig.GOOGLE_MAPS_API_KEY)
            //.withSearchZone("en_US")
            //.withSearchZone(SearchZoneRect(LatLng(26.525467, -18.910366), LatLng(43.906271, 5.394197)))
            //.withDefaultLocaleSearchZone()
            .shouldReturnOkOnBackPressed()
            //.withStreetHidden()
            //.withCityHidden()
            //.withZipCodeHidden()
            //.withSatelliteViewHidden()
            //.withGoogleTimeZoneEnabled()
            //.withVoiceSearchHidden()
            //.withUnnamedRoadHidden()
            .build(ctx)*/
        //activityLauncher.launch(locationPickerIntent)
        val intent = Intent(ctx, LocationPickerActivity::class.java)
        ctx.startActivity(intent)
    }
}