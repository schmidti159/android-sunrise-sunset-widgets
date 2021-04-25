package de.adschmidt.sunrisesunset

import WidgetUpdater
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class WidgetUpdateScheduler() : BroadcastReceiver() {

    companion object {
        private const val UPDATE_INTERVAL_SECONDS = 60L * 7 // every 8 minutes (2 degrees on the circle)

        fun scheduleUpdates(context: Context) {
            val alarmManager: AlarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WidgetUpdateScheduler::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setInexactRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis(),
                1000 * UPDATE_INTERVAL_SECONDS,
                pendingIntent
            )
            Log.i(TAG, "Update timer is set to update every ${1000* UPDATE_INTERVAL_SECONDS}ms")
        }

        fun stopUpdates(context: Context) {
            val intent = Intent(context, WidgetUpdateScheduler::class.java)
            val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(sender)
            Log.i(TAG, "Update timer was cancelled")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "received timing intent. Triggering update for all widgets")
        WidgetUpdater.updateAllWidgets(context)
    }

}