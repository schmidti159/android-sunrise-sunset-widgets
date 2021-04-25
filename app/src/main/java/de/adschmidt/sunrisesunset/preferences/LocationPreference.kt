package de.adschmidt.sunrisesunset.preferences

import WidgetUpdater
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.preference.Preference
import de.adschmidt.sunrisesunset.TAG
import de.adschmidt.sunrisesunset.getDouble
import de.adschmidt.sunrisesunset.locationpicker.LocationPicker
import de.adschmidt.sunrisesunset.model.WidgetPreferences.Companion.DEFAULT_LATITUDE
import de.adschmidt.sunrisesunset.model.WidgetPreferences.Companion.DEFAULT_LONGITUDE
import de.adschmidt.sunrisesunset.putDouble

class LocationPreference(
    private val ctx: Context,
    private val activityLauncher: ActivityResultLauncher<LocationPicker.Params>
) : Preference(ctx) {

    private var address: String? = null

    override fun onAttached() {
        super.onAttached()
        address = sharedPreferences?.getString("$key.address", null)
    }

    fun handleActivityResult(result: LocationPicker.Result) {
        val editor = sharedPreferences.edit()
        editor.putDouble("$key.latitude", result.latitude)
        editor.putDouble("$key.longitude", result.longitude)
        editor.putDouble("$key.altitude", result.altitude)
        editor.putString("$key.address", result.address)
        editor.commit()
        address = result.address
        Log.d(
            TAG,
            "Updated location to $result.address ($result.latitude, $result.longitude, ${result.altitude})"
        )
        notifyChanged()
        WidgetUpdater.updateWidgetForKey(key, context)
    }

    override fun onClick() {
        super.onClick()

        val latitude = sharedPreferences.getDouble("$key.latitude", DEFAULT_LATITUDE)
        val longitude = sharedPreferences.getDouble("$key.longitude", DEFAULT_LONGITUDE)

        activityLauncher.launch(LocationPicker.Params(latitude, longitude))
    }

    class SimpleSummaryProvider(val defaultSummary: CharSequence?) : SummaryProvider<LocationPreference> {
        override fun provideSummary(pref: LocationPreference?): CharSequence? {
            if (pref?.address == null || pref.address!!.isBlank()) {
                return defaultSummary
            } else {
                return pref.address!!
            }
        }
    }
}