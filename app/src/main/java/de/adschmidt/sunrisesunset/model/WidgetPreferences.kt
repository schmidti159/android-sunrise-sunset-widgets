package de.adschmidt.sunrisesunset.model

import android.location.Location

data class WidgetPreferences(
    var customName: String = "",
    var daylightColor: String = "",
    var nightColor: String = "",
    var sunsetColor: String = "",
    var sunriseColor: String = "",
    var location: Location,
    var showSeconds: Boolean = true,
    var showHours: Boolean = false
)