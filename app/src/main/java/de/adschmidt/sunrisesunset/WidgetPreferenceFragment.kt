package de.adschmidt.sunrisesunset

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.preference.*
import androidx.preference.Preference.SummaryProvider
import de.adschmidt.sunrisesunset.model.PreferenceMeta
import de.adschmidt.sunrisesunset.model.PreferenceDataType
import de.adschmidt.sunrisesunset.model.WidgetPreferences
import kotlin.reflect.full.findAnnotation

class WidgetPreferenceFragment(
        private val keyPrefix: String
    ) : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val screen = preferenceManager.createPreferenceScreen(preferenceManager.context)

        val preferencesClass = WidgetPreferences::class
        val prefsByCategory = preferencesClass.members
            .mapNotNull { m -> m.findAnnotation<PreferenceMeta>() }
            .groupBy { p -> p.categoryKey }

        for((categoryKey, prefs) in prefsByCategory.entries.sortedBy { e -> e.key }) {
            addCategoryToScreen(categoryKey, prefs, screen)
        }

        preferenceScreen = screen
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {
        if(key == null ||
                key.split(".").size < 2 ||
                key.split(".")[1].toIntOrNull() == null) {
            // update all widgets
            WidgetUpdater.updateAllWidgets(requireContext())
        } else {
            val widgetId = key.split(".")[1].toInt()
            WidgetUpdater.updateWidget(widgetId, requireContext())
        }
    }

    private fun addCategoryToScreen(categoryKey: String, prefs: List<PreferenceMeta>, screen: PreferenceScreen) {
        val ctx = preferenceManager.context
        val category = PreferenceCategory(ctx)
        category.key = keyPrefix + categoryKey
        category.title =
            getString("widgetPreferences.$categoryKey.title")
        screen.addPreference(category)

        for(pref in prefs.sortedBy { p -> p.key }) {
            addPreferenceToCategory(category, pref)
        }
    }

    private fun addPreferenceToCategory(category: PreferenceCategory, pref: PreferenceMeta) {
        val ctx = preferenceManager.context
        val preference =
            when (pref.dataType) {
                PreferenceDataType.STRING -> buildStringPreference(pref, ctx)
                PreferenceDataType.COLOR -> buildColorPreference(pref, ctx)
                PreferenceDataType.BOOLEAN -> buildBooleanPreference(pref, ctx)
                PreferenceDataType.LOCATION -> buildLocationPreference(pref, ctx)
            }
        if(preference == null) {
            Log.e(TAG, "Could not create a preference from the values in @PreferenceMeta-annotation: $pref")
            return
        }
        preference.key = "$keyPrefix.${pref.key}"
        preference.title = getString("widgetPreferences.${pref.categoryKey}.${pref.key}.title")
        category.addPreference(preference)
    }

    private fun buildStringPreference(pref: PreferenceMeta, ctx: Context): Preference {
        val preference = EditTextPreference(ctx)
        preference.summaryProvider = SummaryProvider<EditTextPreference> {
            if(it.text == null || it.text.isBlank())
                getString("widgetPreferences.${pref.categoryKey}.${pref.key}.summary")
            else
                it.text
        }
        return preference
    }

    private fun buildColorPreference(pref: PreferenceMeta, ctx: Context): Preference {
        // TODO find suitable color picker library
        val preference = EditTextPreference(ctx)
        preference.summaryProvider = SummaryProvider<EditTextPreference> {
            if(it.text == null || it.text.isBlank())
                getString("widgetPreferences.${pref.categoryKey}.${pref.key}.summary")
            else
                it.text
        }
        return preference
    }

    private fun buildBooleanPreference(pref: PreferenceMeta, ctx: Context): Preference {
        val preference = SwitchPreference(ctx)
        preference.summary = getString("widgetPreferences.${pref.categoryKey}.${pref.key}.summary")
        return preference
    }

    private fun buildLocationPreference(pref: PreferenceMeta, ctx: Context): Preference {
        // TODO find/use/build location picker
        val preference = EditTextPreference(ctx)
        preference.summaryProvider = SummaryProvider<EditTextPreference> {
            if(it.text == null || it.text.isBlank())
                getString("widgetPreferences.${pref.categoryKey}.${pref.key}.summary")
            else
                it.text
        }
        return preference
    }

    private fun getString(key: String): CharSequence? {
        val ctx = preferenceManager.context
        val resourceId = resources.getIdentifier(key, "string", ctx.packageName)
        return if(resourceId == 0)
                "!MISSING String: $key!"
            else
                ctx.getString(resourceId)
    }

}