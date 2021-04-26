package de.adschmidt.sunrisesunset

import WidgetUpdater
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.util.Log
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
            Log.i(TAG, "Updating widget $appWidgetId")
            WidgetUpdater.updateWidget(appWidgetId, context)
        }
        Log.i(TAG, "Starting the update scheduler from onUpdate(..) just to be sure.")
        WidgetUpdateScheduler.scheduleUpdates(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            Log.i(TAG, "Deleting widget $appWidgetId")
            WidgetSizeProvider.delete(appWidgetId, context)
            WidgetPreferenceProvider.delete(appWidgetId, context)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        Log.i(TAG, "Enabled the first widget. Starting the update scheduler.")
        WidgetUpdateScheduler.scheduleUpdates(context)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        Log.i(TAG, "Disabled the last widget. Stopping the scheduler.")
        WidgetUpdateScheduler.stopUpdates(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        Log.i(TAG, "Updating the widget options for widget $appWidgetId")
        WidgetSizeProvider.updateSize(appWidgetId, newOptions, context)
        onUpdate(context, appWidgetManager!!, intArrayOf(appWidgetId))
    }
}

