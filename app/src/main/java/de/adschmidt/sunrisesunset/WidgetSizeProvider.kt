package de.adschmidt.sunrisesunset

import android.appwidget.AppWidgetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log

class WidgetSizeProvider {

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

    val widgetSizePerId: MutableMap<Int, WidgetSize> = HashMap()

    fun getSize(widgetId: Int): WidgetSize? {
        return widgetSizePerId[widgetId]
    }

    fun updateOnResize(widgetId: Int, widgetInfo: Bundle?) {
        if (widgetInfo == null) {
            Log.e(TAG, "got null widgetInfo for widgetId: $widgetId")
            return
        }
        val size = WidgetSize(
            widgetInfo.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH),
            widgetInfo.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH),
            widgetInfo.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT),
            widgetInfo.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        )
        Log.i(TAG, "set widget size for widgetId: $widgetId to $size")
        widgetSizePerId[widgetId] = size
    }
}