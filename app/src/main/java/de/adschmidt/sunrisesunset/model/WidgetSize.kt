package de.adschmidt.sunrisesunset.model

import android.content.res.Configuration
import android.content.res.Resources

data class WidgetSize(
    val maxWidth: Int,
    val minWidth: Int,
    val maxHeight: Int,
    val minHeight: Int
) {
    fun getWidth(resources: Resources): Int {
        val isPortrait =
            resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        return if (isPortrait) minWidth else maxWidth
    }

    fun getHeight(resources: Resources): Int {
        val isPortrait =
            resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        return if (isPortrait) maxHeight else minHeight
    }
}