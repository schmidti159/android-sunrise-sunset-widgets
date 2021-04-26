package de.adschmidt.sunrisesunset.persistence

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceManager
import de.adschmidt.sunrisesunset.TAG
import de.adschmidt.sunrisesunset.model.WidgetSize

object WidgetSizeProvider {
    const val SHARED_PREFERENCES_PREFIX = "widgetSizes"

    fun getSize(widgetId: Int, ctx: Context): WidgetSize? {
        val keyPrefix = "${WidgetPreferenceProvider.SHARED_PREFERENCES_PREFIX}.${widgetId}"
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        val size = WidgetSize(
            sharedPrefs.getInt("${keyPrefix}.max.width", -1),
            sharedPrefs.getInt("${keyPrefix}.min.width", -1),
            sharedPrefs.getInt("${keyPrefix}.max.height", -1),
            sharedPrefs.getInt("${keyPrefix}.min.height", -1)
        )
        return if(size.isValid()) size else null
    }

    fun updateSize(widgetId: Int, widgetInfo: Bundle?, ctx: Context) {
        if (widgetInfo == null) {
            Log.e(TAG, "got null widgetInfo for widgetId: $widgetId")
            return
        }
        val keyPrefix = "${WidgetPreferenceProvider.SHARED_PREFERENCES_PREFIX}.${widgetId}"
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        val editor = sharedPrefs.edit()
        editor.putInt("${keyPrefix}.max.width", widgetInfo.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH))
        editor.putInt("${keyPrefix}.min.width", widgetInfo.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH))
        editor.putInt("${keyPrefix}.max.height", widgetInfo.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT))
        editor.putInt("${keyPrefix}.min.height", widgetInfo.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT))
        if(!editor.commit()) {
            Log.e(TAG, "could not save sizes for widget $widgetId")
        }

        Log.i(TAG, "saved sizes for widget $widgetId")
    }

    fun delete(widgetId: Int, ctx: Context) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        val editor = sharedPrefs.edit()

        sharedPrefs.all
            .map { entry -> entry.key }
            .filter { key -> key.startsWith("${WidgetPreferenceProvider.SHARED_PREFERENCES_PREFIX}.$widgetId.") }
            .forEach { key -> editor.remove(key) }

        if(!editor.commit()) {
            Log.e(TAG, "could not delete sizes for widget $widgetId")
        }

        Log.i(TAG, "deleted sizes for widget $widgetId")
    }
}