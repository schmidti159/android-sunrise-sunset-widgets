package de.adschmidt.sunrisesunset.preferences

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import de.adschmidt.sunrisesunset.R
import de.adschmidt.sunrisesunset.TAG
import de.adschmidt.sunrisesunset.persistence.WidgetPreferenceProvider.SHARED_PREFERENCES_PREFIX

class WidgetPreferenceActivity : AppCompatActivity() {
    companion object {
        const val KEY_WIDGET_ID = "widgetId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val widgetId = intent.extras?.getInt(KEY_WIDGET_ID)
        Log.i(TAG, "creating WidgetPreferenceActivity for widget $widgetId")

        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, WidgetPreferenceFragment("$SHARED_PREFERENCES_PREFIX.$widgetId"))
                .commit()
        }
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

}