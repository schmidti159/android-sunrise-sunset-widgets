package de.adschmidt.sunrisesunset.model

import android.location.Location
import androidx.annotation.ColorInt

data class WidgetPreferences(
    var widgetId: Int = 0,
    @PreferenceMeta(key="00_name", dataType=PreferenceDataType.STRING, categoryKey = "00_general")
    var customName: String = "",
    @PreferenceMeta(key="00_daylightColor", dataType=PreferenceDataType.COLOR, categoryKey = "10_colors")
    @ColorInt
    var daylightColor: Int = 0xffffff00.toInt(),
    @PreferenceMeta(key="10_nightColor", dataType=PreferenceDataType.COLOR, categoryKey = "10_colors")
    @ColorInt
    var nightColor: Int = 0xff0000ff.toInt(),
    @PreferenceMeta(key="20_sunsetColor", dataType=PreferenceDataType.COLOR, categoryKey = "10_colors")
    @ColorInt
    var sunsetColor: Int = 0xffff0000.toInt(),
    @PreferenceMeta(key="30_sunriseColor", dataType=PreferenceDataType.COLOR, categoryKey = "10_colors")
    @ColorInt
    var sunriseColor: Int = 0xffffa500.toInt(),
    @PreferenceMeta(key="40_backgroundColor", dataType=PreferenceDataType.COLOR, categoryKey = "10_colors")
    @ColorInt
    var backgroundColor: Int = 0x55808080.toInt(),
    @PreferenceMeta(key="50_markerColor", dataType=PreferenceDataType.COLOR, categoryKey = "10_colors")
    @ColorInt
    var markerColor: Int = 0xffffffff.toInt(),
    @PreferenceMeta(key="60_clockColor", dataType=PreferenceDataType.COLOR, categoryKey = "10_colors")
    @ColorInt
    var clockColor: Int = 0xffffffff.toInt(),
    @PreferenceMeta(key="10_location", dataType=PreferenceDataType.LOCATION, categoryKey = "00_general")
    var location: Location,
    @PreferenceMeta(key="10_showSeconds", dataType=PreferenceDataType.BOOLEAN, categoryKey = "20_clock")
    var showSeconds: Boolean = true,
    @PreferenceMeta(key="20_use24Hours", dataType=PreferenceDataType.BOOLEAN, categoryKey = "20_clock")
    var use24Hours: Boolean = false
) {
    companion object {
        val DEFAULT_PREFS = WidgetPreferences(
            location = Location("")
        )
        init {
            // initialize location with Bambergs coordinates
            DEFAULT_PREFS.location.latitude = Location.convert("49.891415011500726")
            DEFAULT_PREFS.location.longitude = Location.convert("10.907930440129253")
        }
    }
}
