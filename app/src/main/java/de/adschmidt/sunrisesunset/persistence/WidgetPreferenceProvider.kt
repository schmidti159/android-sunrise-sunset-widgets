package de.adschmidt.sunrisesunset.persistence

import de.adschmidt.sunrisesunset.model.WidgetPreferences

object WidgetPreferenceProvider {
    // dummy implementation
    // TODO store in DB or at least sharedPreferences
    private val preferenceMap: MutableMap<Int, WidgetPreferences> = HashMap()

    fun getPreferencs(widgetId: Int) : WidgetPreferences? {
        return preferenceMap[widgetId]
    }

    fun updatePreferences(widgetId: Int, prefs: WidgetPreferences) {
        preferenceMap[widgetId] = prefs
    }

    fun delete(widgetId: Int) {
        preferenceMap.remove(widgetId)
    }

}