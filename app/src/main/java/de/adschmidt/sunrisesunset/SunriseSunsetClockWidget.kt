package de.adschmidt.sunrisesunset

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.*
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import de.adschmidt.sunrisesunset.calc.TimeCalculator
import net.time4j.Moment
import net.time4j.PlainDate
import net.time4j.TemporalType
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
        val padding = 10
        val radius = (min(width, height) / 2).toInt() - 4 * padding

        // draw the circle
        val location = Location("")
        location.latitude = Location.convert("49.891415011500726")
        location.longitude = Location.convert("10.907930440129253")
        val imageBitmap = drawBackgroundCircle(radius, padding, location)
        views.setImageViewBitmap(R.id.clock_widget_background, imageBitmap)

        // set the time
        val today = PlainDate.nowInSystemTime()
        val timeStampForZero =
            TemporalType.MILLIS_SINCE_UNIX.from(today.atStartOfDay().inStdTimezone())
        val baseTimeStamp =
            SystemClock.elapsedRealtime() - System.currentTimeMillis() + timeStampForZero
        //views.setChronometer(R.id.clock_widget_chronometer, baseTimeStamp, "%s", true)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun drawBackgroundCircle(radius: Int, padding: Int, location: Location): Bitmap {
        val boundingBox = RectF(
            padding.toFloat(),
            padding.toFloat(),
            ((2 * radius) + padding).toFloat(),
            ((2 * radius) + padding).toFloat()
        )
        val bitmap = Bitmap.createBitmap(
            2 * (radius + padding),
            2 * (radius + padding),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        val dayColor = Paint(Paint.ANTI_ALIAS_FLAG)
        dayColor.color = Color.YELLOW
        dayColor.style = Paint.Style.STROKE
        dayColor.strokeWidth = 10F
        dayColor.strokeCap = Paint.Cap.ROUND

        val sunriseColor = Paint(dayColor)
        sunriseColor.color = Color.rgb(255, 165, 0)

        val sunsetColor = Paint(dayColor)
        sunsetColor.color = Color.RED

        val nightColor = Paint(dayColor)
        nightColor.color = Color.BLUE

        // background
        val backgroundColor = Paint(dayColor)
        backgroundColor.color = Color.argb(64, 128, 128, 128)
        backgroundColor.style = Paint.Style.FILL_AND_STROKE
        canvas.drawCircle(
            (radius + padding).toFloat(),
            (radius + padding).toFloat(),
            (radius).toFloat(),
            backgroundColor
        )

        // the circle
        val solarTimes = timeCalculator.solarTimeForLocation(location)
        val times = timeCalculator.getTimesForDate(solarTimes, PlainDate.nowInSystemTime())
        Log.i(TAG, "solarTimes: " + times)

        val sunriseDeg = toDeg(times.sunrise)
        val sunsetDeg = toDeg(times.sunset)
        val sunsetTwilightDeg = toDeg(times.sunsetTwilight)
        val sunriseTwilightDeg = toDeg(times.sunriseTwilight)

        Log.i(TAG, "sunrise: " + times.sunrise + " deg: " + sunriseDeg)
        Log.i(TAG, "sunset: " + times.sunset + " deg: " + sunsetDeg)
        Log.i(TAG, "sunsetTwilight: " + times.sunsetTwilight + " deg: " + sunsetTwilightDeg)
        Log.i(TAG, "sunriseTwilight: " + times.sunriseTwilight + " deg: " + sunriseTwilightDeg)
        canvas.drawArc(boundingBox, sunriseDeg, distance(sunriseDeg, sunsetDeg), false, dayColor)
        canvas.drawArc(
            boundingBox,
            sunsetTwilightDeg,
            distance(sunsetTwilightDeg, sunriseTwilightDeg),
            false,
            nightColor
        )
        canvas.drawArc(
            boundingBox,
            sunsetDeg,
            distance(sunsetDeg, sunsetTwilightDeg),
            false,
            sunsetColor
        )
        canvas.drawArc(
            boundingBox,
            sunriseTwilightDeg,
            distance(sunriseTwilightDeg, sunriseDeg),
            false,
            sunriseColor
        )
        // draw the marker on the circle
        val markerDeg = toDeg(Moment.nowInSystemTime())
        val markerRad = markerDeg * PI / 180.0
        val xPosition = cos(markerRad)
        val yPosition = sin(markerRad)

        val markerColor = Paint(dayColor)
        markerColor.color = Color.WHITE
        markerColor.style = Paint.Style.FILL_AND_STROKE
        canvas.drawCircle(
            (radius * xPosition + radius + padding).toFloat(),
            (radius * yPosition + radius + padding).toFloat(),
            10F,
            markerColor
        )


        return bitmap
    }

    private fun toDeg(moment: Moment): Float {
        val localTime = moment.inLocalView().toTimestamp().wallTime
        val timeInSeconds = localTime.second + localTime.minute * 60 + localTime.hour * 60 * 60
        val degreeFromMidnight = timeInSeconds / (24F * 60 * 60) * 360
        val degree = degreeFromMidnight - 270F // 0 degree is at 3 o'clock
        if (degree < 0) {
            return degree + 360
        } else {
            return degree
        }
    }

    private fun distance(degFrom: Float, degTo: Float): Float {
        if (degFrom <= degTo) {
            return degTo - degFrom
        } else {
            return 360 - (degFrom - degTo)
        }
    }
}

