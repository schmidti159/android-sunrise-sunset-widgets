package de.adschmidt.sunrisesunset.model

import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location

data class WidgetPreferences(
    var customName: String = "",
    var daylightColor: String = "#ffff00",
    var nightColor: String = "#0000ff",
    var sunsetColor: String = "#ff0000",
    var sunriseColor: String = "#ffa500",
    var backgroundColor: String = "#55808080",
    var markerColor: String = "#ffffff",
    var location: Location,
    var showSeconds: Boolean = true,
    var showHours: Boolean = false
) {
    companion object {
        val DEFAULT_PREFS = WidgetPreferences(
            location = Location("")
        )
        init {
            // initialize location with Bambergs coordinates
            DEFAULT_PREFS.location.latitude = Location.convert("49.891415011500726")
            DEFAULT_PREFS.location.longitude = Location.convert("10.907930440129253")
        }
    }
}
