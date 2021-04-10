package de.adschmidt.sunrisesunset.persistence

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.preference.PreferenceManager
import de.adschmidt.sunrisesunset.TAG
import de.adschmidt.sunrisesunset.model.PreferenceDataType
import de.adschmidt.sunrisesunset.model.PreferenceMeta
import de.adschmidt.sunrisesunset.model.WidgetPreferences
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

object WidgetPreferenceProvider {
    const val SHARED_PREFERENCES_PREFIX = "widgetPreferences"

    fun getWidgetIds(ctx: Context): Set<Int> {
        return readWidgetIds(ctx)
    }

    fun getPreferencs(widgetId: Int, ctx: Context) : WidgetPreferences? {
        return readPreferences(widgetId, ctx)
    }

    fun updatePreferences(prefs: WidgetPreferences, ctx: Context) {
        savePreferences(prefs, ctx);
    }

    fun delete(widgetId: Int, ctx: Context) {
        deletePreferences(widgetId, ctx)
    }

    private fun readWidgetIds(ctx: Context): Set<Int> {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx)

        val widgetIds = sharedPrefs.all
            .map { entry -> entry.key }
            .filter { key -> key.startsWith("$SHARED_PREFERENCES_PREFIX.") }
            .map { key -> key.split(".")[1] }
            .mapNotNull { key -> key.toIntOrNull()}
            .toSet()
        Log.i(TAG, "found widgetIds in SharedPreferences: $widgetIds")
        return widgetIds
    }

    private fun savePreferences(prefs: WidgetPreferences, ctx: Context) {
        val membersToSave = prefs::class.memberProperties
            .filter { m -> m.findAnnotation<PreferenceMeta>() != null }

        val keyPrefix = "$SHARED_PREFERENCES_PREFIX.${prefs.widgetId}"
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        val editor = sharedPrefs.edit()

        for(member in membersToSave) {
            val prefMeta = member.findAnnotation<PreferenceMeta>()
            if(prefMeta == null) {
                Log.w(TAG, "Cannot save member ${member.name} because the @PreferenceMeta annotation cannot be read")
                continue
            }
            val key = "$keyPrefix.${prefMeta.key}"
            when {
                PreferenceDataType.STRING == prefMeta.dataType -> {
                    editor.putString(key, member.getter.call(prefs) as String?)
                }
                PreferenceDataType.COLOR == prefMeta.dataType -> {
                    editor.putInt(key, member.getter.call(prefs) as Int)
                }
                PreferenceDataType.BOOLEAN == prefMeta.dataType -> {
                    editor.putBoolean(key, member.getter.call(prefs) as Boolean)
                }
                PreferenceDataType.LOCATION == prefMeta.dataType -> {
                    val location = member.getter.call(prefs) as Location
                    editor.putFloat("$key.latitude", location.latitude.toFloat())
                    editor.putFloat("$key.longitude", location.longitude.toFloat())
                    editor.putFloat("$key.altitude", location.altitude.toFloat())
                    editor.putString("$key.provider", location.provider)
                }
            }
        }

        if(!editor.commit()) {
            Log.e(TAG, "could not save prefs for widget ${prefs.widgetId}")
        }
        Log.i(TAG, "saved prefs for widget ${prefs.widgetId}")
    }

    private fun readPreferences(widgetId: Int, ctx: Context) : WidgetPreferences {
        val prefs = WidgetPreferences.DEFAULT_PREFS
        prefs.widgetId = widgetId
        val membersToRead = prefs::class.memberProperties
            .filter { m -> m.findAnnotation<PreferenceMeta>() != null }

        val keyPrefix = "$SHARED_PREFERENCES_PREFIX.${widgetId}"
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx)

        for(member in membersToRead) {
            val prefMeta = member.findAnnotation<PreferenceMeta>()
            if(prefMeta == null) {
                Log.w(TAG, "Cannot read member ${member.name} because the @PreferenceMeta annotation cannot be read")
                continue
            }
            if(member !is KMutableProperty<*>) {
                Log.w(TAG, "Cannot read member ${member.name} because it is not mutable")
                continue
            }
            val key = "$keyPrefix.${prefMeta.key}"
            when {
                PreferenceDataType.STRING == prefMeta.dataType -> {
                    member.setter.call(prefs, sharedPrefs.getString(key, member.getter.call(prefs) as String?))
                }
                PreferenceDataType.COLOR == prefMeta.dataType -> {
                    member.setter.call(prefs, sharedPrefs.getInt(key, member.getter.call(prefs) as Int))
                }
                PreferenceDataType.BOOLEAN == prefMeta.dataType -> {
                    member.setter.call(prefs, sharedPrefs.getBoolean(key, member.getter.call(prefs) as Boolean))
                }
                PreferenceDataType.LOCATION == prefMeta.dataType -> {
                    val latitude = sharedPrefs.getFloat("$key.latitude", Float.NaN);
                    val longitude = sharedPrefs.getFloat("$key.longitude", Float.NaN);
                    val altitude = sharedPrefs.getFloat("$key.altitude", Float.NaN);
                    val provider = sharedPrefs.getString("$key.provider", null);
                    if(!(latitude.isNaN() || longitude.isNaN() || altitude.isNaN() || provider == null)) {
                        val location = Location(provider)
                        location.latitude = latitude.toDouble()
                        location.longitude = longitude.toDouble()
                        location.altitude = altitude.toDouble()
                        member.setter.call(prefs, location)
                    }
                }
            }
        }
        Log.i(TAG, "read prefs for widget $widgetId")
        return prefs
    }

    private fun deletePreferences(widgetId: Int, ctx: Context) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        val editor = sharedPrefs.edit()

        sharedPrefs.all
            .map { entry -> entry.key }
            .filter { key -> key.startsWith("$SHARED_PREFERENCES_PREFIX.$widgetId.") }
            .forEach { key -> editor.remove(key) }

        if(!editor.commit()) {
            Log.e(TAG, "could not delete prefs for widget $widgetId")
        }

        Log.i(TAG, "deleted prefs for widget $widgetId")
    }

}