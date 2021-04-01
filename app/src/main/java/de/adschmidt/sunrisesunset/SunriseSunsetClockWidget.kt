package de.adschmidt.sunrisesunset

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.*
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import de.adschmidt.sunrisesunset.calc.TimeCalculator
import de.adschmidt.sunrisesunset.model.WidgetPreferences
import net.time4j.Moment
import net.time4j.PlainDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [SunriseSunsetClockWidgetConfigureActivity]
 */
class SunriseSunsetClockWidget : AppWidgetProvider() {
    private val timeCalculator = TimeCalculator()
    private val widgetSizeProvider = WidgetSizeProvider()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            if (widgetSizeProvider.getSize(appWidgetId) == null) {
                widgetSizeProvider.updateOnResize(
                    appWidgetId,
                    appWidgetManager.getAppWidgetOptions(appWidgetId)
                )
            }
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteTitlePref(context, appWidgetId)
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
        widgetSizeProvider.updateOnResize(appWidgetId, newOptions)
        onUpdate(context!!, appWidgetManager!!, intArrayOf(appWidgetId))
    }

    internal fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        //
        val location = Location("")
        location.latitude = Location.convert("49.891415011500726")
        location.longitude = Location.convert("10.907930440129253")
        val widgetPreferences = WidgetPreferences(showSeconds = true, location = location)

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.sunrise_sunset_clock_widget)

        // init size values
        val size = widgetSizeProvider.getSize(appWidgetId)
        if (size == null) {
            Log.e(TAG, "widgetSize for widget $appWidgetId was not initialized")
            return
        }
        val width = size.getWidth(context.resources) * context.resources.displayMetrics.density
        val height = size.getHeight(context.resources) * context.resources.displayMetrics.density
        val maxRadius = min(width, height) / 2
        val padding = min(10F, maxRadius / 10)
        val radius = (min(width, height) / 2).toInt() - 4 * padding
        Log.i(
            TAG,
            "drawing widget for width: $width and height: $height, radius: $radius, padding: $padding"
        )

        // draw the circle
        val imageBitmap = drawBackgroundCircle(radius, padding, widgetPreferences.location)
        views.setImageViewBitmap(R.id.clock_widget_background, imageBitmap)

        // configure the clock
        if (widgetPreferences.showSeconds) {
            views.setCharSequence(R.id.clock_widget_clock, "setFormat24Hour", "kk:mm:ss")
            views.setCharSequence(R.id.clock_widget_clock, "setFormat12Hour", "kk:mm:ss")
        } else {
            views.setCharSequence(R.id.clock_widget_clock, "setFormat24Hour", "kk:mm")
            views.setCharSequence(R.id.clock_widget_clock, "setFormat12Hour", "kk:mm")
        }
        val clockFontSize = getFontSize(radius, padding, widgetPreferences)
        Log.i(TAG, "setting fontsize: $clockFontSize")
        views.setFloat(R.id.clock_widget_clock, "setTextSize", clockFontSize)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getFontSize(radius: Float, padding: Float, prefs: WidgetPreferences): Float {
        val maxWidth = 2 * (radius)
        val charCount = if (prefs.showSeconds) 8 else 5 // kk:mm:ss or kk:mm
        Log.i(TAG, "maxWidth for Text: $maxWidth, per char: ${maxWidth / charCount}")
        return ((maxWidth / charCount * 0.7)).toFloat()
    }

    private fun drawBackgroundCircle(radius: Float, padding: Float, location: Location): Bitmap {
        val circleBox = RectF(padding, padding, ((2 * radius) + padding), ((2 * radius) + padding))
        val bitmap = Bitmap.createBitmap(
            (2 * (radius + padding)).toInt(),
            (2 * (radius + padding)).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        val dayColor = Paint(Paint.ANTI_ALIAS_FLAG)
        dayColor.color = Color.YELLOW
        dayColor.style = Paint.Style.STROKE
        dayColor.strokeWidth = padding / 1.5F
        dayColor.strokeCap = Paint.Cap.ROUND

        val sunriseColor = Paint(dayColor)
        sunriseColor.color = Color.rgb(255, 165, 0)

        val sunsetColor = Paint(dayColor)
        sunsetColor.color = Color.RED

        val nightColor = Paint(dayColor)
        nightColor.color = Color.BLUE

        // background
        val backgroundColor = Paint(dayColor)
        backgroundColor.color = Color.argb(85, 128, 128, 128)
        backgroundColor.style = Paint.Style.FILL_AND_STROKE
        canvas.drawCircle((radius + padding), (radius + padding), radius, backgroundColor)

        // the circle
        val solarTimes = timeCalculator.solarTimeForLocation(location)
        val times = timeCalculator.getTimesForDate(solarTimes, PlainDate.nowInSystemTime())
        Log.i(TAG, "solarTimes: $times")

        val sunriseDeg = toDeg(times.sunrise)
        val sunsetDeg = toDeg(times.sunset)
        val sunset2Deg = toDeg(times.sunsetTwilight)
        val sunrise2Deg = toDeg(times.sunriseTwilight)

        Log.i(TAG, "sunrise: " + times.sunrise + " deg: " + sunriseDeg)
        Log.i(TAG, "sunset: " + times.sunset + " deg: " + sunsetDeg)
        Log.i(TAG, "sunsetTwilight: " + times.sunsetTwilight + " deg: " + sunset2Deg)
        Log.i(TAG, "sunriseTwilight: " + times.sunriseTwilight + " deg: " + sunrise2Deg)
        canvas.drawArc(circleBox, sunriseDeg, dist(sunriseDeg, sunsetDeg), false, dayColor)
        canvas.drawArc(circleBox, sunset2Deg, dist(sunset2Deg, sunrise2Deg), false, nightColor)
        canvas.drawArc(circleBox, sunsetDeg, dist(sunsetDeg, sunset2Deg), false, sunsetColor)
        canvas.drawArc(circleBox, sunrise2Deg, dist(sunrise2Deg, sunriseDeg), false, sunriseColor)

        // draw the marker on the circle
        val markerDeg = toDeg(Moment.nowInSystemTime())
        val markerRad = markerDeg * PI / 180.0
        val xPosition = (radius * cos(markerRad) + radius + padding).toFloat()
        val yPosition = (radius * sin(markerRad) + radius + padding).toFloat()

        val markerColor = Paint(dayColor)
        markerColor.color = Color.WHITE
        markerColor.style = Paint.Style.FILL_AND_STROKE
        canvas.drawCircle(xPosition, yPosition, padding / 1.5F, markerColor)

        return bitmap
    }

    private fun toDeg(moment: Moment): Float {
        val localTime = moment.inLocalView().toTimestamp().wallTime
        val timeInSeconds = localTime.second + localTime.minute * 60 + localTime.hour * 60 * 60
        val degreeFromMidnight = timeInSeconds / (24F * 60 * 60) * 360
        val degree = degreeFromMidnight - 270F // 0 degree is at 3 o'clock
        return if (degree < 0) {
            degree + 360
        } else {
            degree
        }
    }

    private fun dist(degFrom: Float, degTo: Float): Float {
        return if (degFrom <= degTo) {
            degTo - degFrom
        } else {
            360 - (degFrom - degTo)
        }
    }
}

