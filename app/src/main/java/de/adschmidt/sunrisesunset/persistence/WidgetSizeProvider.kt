package de.adschmidt.sunrisesunset.persistence

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.util.Log
import de.adschmidt.sunrisesunset.TAG
import de.adschmidt.sunrisesunset.model.WidgetSize

object WidgetSizeProvider {

    // TODO save in DB or as SharedPreferences
    private val widgetSizePerId: MutableMap<Int, WidgetSize> = HashMap()

    fun getSize(widgetId: Int): WidgetSize? {
        return widgetSizePerId[widgetId]
    }

    fun updateSize(widgetId: Int, widgetInfo: Bundle?) {
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

    fun delete(widgetId: Int) {
        widgetSizePerId.remove(widgetId)
    }
}