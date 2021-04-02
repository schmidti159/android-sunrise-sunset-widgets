package de.adschmidt.sunrisesunset

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import de.adschmidt.sunrisesunset.model.WidgetPreferences
import de.adschmidt.sunrisesunset.persistence.WidgetPreferenceProvider
import de.adschmidt.sunrisesunset.persistence.WidgetSizeProvider

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [ClockWidgetConfigureActivity]
 */
class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            if(WidgetSizeProvider.getSize(appWidgetId) == null) {
                val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                WidgetSizeProvider.updateSize(appWidgetId, options)
            }
            if(WidgetPreferenceProvider.getPreferencs(appWidgetId) == null) {
                WidgetPreferenceProvider.updatePreferences(appWidgetId, WidgetPreferences.DEFAULT_PREFS)
            }
            WidgetUpdater.updateWidget(appWidgetId, context)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            WidgetSizeProvider.delete(appWidgetId)
            WidgetPreferenceProvider.delete(appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        WidgetSizeProvider.updateSize(appWidgetId, newOptions)
        onUpdate(context!!, appWidgetManager!!, intArrayOf(appWidgetId))
    }
}

