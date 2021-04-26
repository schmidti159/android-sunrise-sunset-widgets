package de.adschmidt.sunrisesunset.model

import android.content.Context

data class WidgetContext(
    val prefs: WidgetPreferences,
    val size: WidgetSize,
    val androidCtx: Context
) {
    var times: Times = Times()
    var radius: Float = 0F
    var padding: Float = 0F
}
