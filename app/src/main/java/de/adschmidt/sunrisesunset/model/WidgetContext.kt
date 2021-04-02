package de.adschmidt.sunrisesunset.model

import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location

data class WidgetContext(
    val prefs: WidgetPreferences,
    val size: WidgetSize
) {
    var times: Times = Times()
    var radius: Float = 0F
    var padding: Float = 0F
}
