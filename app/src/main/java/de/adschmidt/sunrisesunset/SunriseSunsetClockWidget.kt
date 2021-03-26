package de.adschmidt.sunrisesunset

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.*
import android.location.Location
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import de.adschmidt.sunrisesunset.calc.TimeCalculator
import net.time4j.Moment
import net.time4j.PlainDate
import net.time4j.TemporalType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [SunriseSunsetClockWidgetConfigureActivity]
 */
class SunriseSunsetClockWidget : AppWidgetProvider() {
    private val timeCalculator = TimeCalculator()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
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

    internal fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val widgetText = loadTitlePref(context, appWidgetId) + appWidgetId
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.sunrise_sunset_clock_widget)
        views.setTextViewText(R.id.appwidget_text, widgetText)

        // draw the circle
        val location = Location("")
        location.latitude = Location.convert("49.891415011500726")
        location.longitude = Location.convert("10.907930440129253")
        val imageBitmap = drawBackgroundCircle(500, 500, location)
        views.setImageViewBitmap(R.id.clock_widget_background, imageBitmap)

        // set the time
        val today = PlainDate.nowInSystemTime()
        val timeStampForZero =
            TemporalType.MILLIS_SINCE_UNIX.from(today.atStartOfDay().inStdTimezone())
        val baseTimeStamp =
            SystemClock.elapsedRealtime() - System.currentTimeMillis() + timeStampForZero
        views.setChronometer(R.id.clock_widget_chronometer, baseTimeStamp, null, true)

        // draw the marker on the circle
        val markerDeg = toDeg(Moment.nowInSystemTime())
        val markerRad = markerDeg * PI / 180.0
        val radius = 150 * 2//context.resources.displayMetrics.density
        val xPosition = cos(markerRad)
        val yPosition = sin(markerRad)
        val leftPadding = (radius * xPosition + radius).toInt()
        val topPadding = (radius * yPosition + radius).toInt()
        val rightPadding = (radius * (-xPosition) + radius).toInt()
        val bottomPadding = (radius * (-yPosition) + radius).toInt()
        Log.i(
            TAG,
            "positioning marker with padding (l,t,r,b): ($leftPadding, $topPadding, $rightPadding, $bottomPadding)"
        )
        views.setViewPadding(
            R.id.clock_widget_time_marker,
            leftPadding,
            topPadding,
            rightPadding,
            bottomPadding
        )

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun drawBackgroundCircle(width: Int, height: Int, location: Location): Bitmap {
        val boundingBox = RectF(10F, 10F, width - 10F, height - 10F)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val dayColor = Paint(Paint.ANTI_ALIAS_FLAG)
        dayColor.color = Color.YELLOW
        dayColor.style = Paint.Style.STROKE
        dayColor.strokeWidth = 5f

        val sunriseColor = Paint(dayColor)
        sunriseColor.color = Color.rgb(255, 165, 0)

        val sunsetColor = Paint(dayColor)
        sunsetColor.color = Color.RED

        val nightColor = Paint(dayColor)
        nightColor.color = Color.BLUE

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
            sunsetDeg,
            distance(sunsetDeg, sunsetTwilightDeg),
            false,
            sunsetColor
        )
        canvas.drawArc(
            boundingBox,
            sunsetTwilightDeg,
            distance(sunsetTwilightDeg, sunriseTwilightDeg),
            false,
            nightColor
        )
        canvas.drawArc(
            boundingBox,
            sunriseTwilightDeg,
            distance(sunriseTwilightDeg, sunriseDeg),
            false,
            sunriseColor
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

