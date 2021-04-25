import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.*
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import de.adschmidt.sunrisesunset.R
import de.adschmidt.sunrisesunset.TAG
import de.adschmidt.sunrisesunset.calc.TimeCalculator
import de.adschmidt.sunrisesunset.model.WidgetContext
import de.adschmidt.sunrisesunset.model.WidgetPreferences
import de.adschmidt.sunrisesunset.persistence.WidgetPreferenceProvider
import de.adschmidt.sunrisesunset.persistence.WidgetSizeProvider
import net.time4j.Moment
import net.time4j.PlainDate
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object WidgetUpdater {
    private val timeCalculator : TimeCalculator = TimeCalculator()

    fun updateAllWidgets(context: Context) {
        val widgetIds = WidgetPreferenceProvider.getWidgetIds(context)
        for (widgetId in widgetIds) {
            updateWidget(widgetId, context)
        }
    }

    fun updateWidget(widgetId: Int, androidContext: Context) {
        val ctx = initWidgetContext(widgetId, androidContext)

        // Construct the RemoteViews object
        val views = RemoteViews(androidContext.packageName, R.layout.sunrise_sunset_clock_widget)

        updateCircle(ctx, views)
        updateClock(ctx, views)

        // Instruct the widget manager to update the widget
        val appWidgetManager = AppWidgetManager.getInstance(androidContext)
        appWidgetManager.updateAppWidget(widgetId, views)
    }

    fun updateWidgetForKey(key: String?, androidContext: Context) {
        if(key == null ||
            key.split(".").size < 2 ||
            key.split(".")[1].toIntOrNull() == null) {
            // cannot find out which widget should be updated -> update all
            WidgetUpdater.updateAllWidgets(androidContext)
        } else {
            val widgetId = key.split(".")[1].toInt()
            WidgetUpdater.updateWidget(widgetId, androidContext)
        }
    }

    private fun initWidgetContext(widgetId: Int, androidContext: Context) : WidgetContext {
        val widgetExists = WidgetPreferenceProvider.getWidgetIds(androidContext)
            .any { id -> widgetId == id }
        if(!widgetExists) {
            Log.w(TAG, "Preferences for $widgetId are not initialized. Using default preferences.")
            val initialPrefs = WidgetPreferences.DEFAULT_PREFS
            initialPrefs.widgetId = widgetId
            WidgetPreferenceProvider.updatePreferences(WidgetPreferences.DEFAULT_PREFS, androidContext)
        }
        val prefs = WidgetPreferenceProvider.getPreferencs(widgetId, androidContext)
            ?: throw IllegalStateException("Preferences for $widgetId are still not initialized after updating them")

        var size = WidgetSizeProvider.getSize(widgetId)
        if(size == null) {
            Log.w(TAG, "Sizes for $widgetId are not initialized. Reading sizes from the widgetManager.")
            val widgetOptions = AppWidgetManager.getInstance(androidContext).getAppWidgetOptions(widgetId)
            WidgetSizeProvider.updateSize(widgetId, widgetOptions)
            size = WidgetSizeProvider.getSize(widgetId)
                ?: throw IllegalStateException("Sizes for $widgetId are still not initialized after updating them")
        }

        val ctx = WidgetContext(prefs, size)
        // calculate the radius and padding
        val width = size.getWidth(androidContext.resources) * androidContext.resources.displayMetrics.density
        val height = size.getHeight(androidContext.resources) * androidContext.resources.displayMetrics.density
        val maxRadius = min(width, height) / 2
        ctx.padding = min(10F, maxRadius / 10)
        ctx.radius = (min(width, height) / 2).toInt() - 4 * ctx.padding
        Log.i(
            TAG,
            "drawing widget for width: $width and height: $height, radius: ${ctx.radius}, padding: ${ctx.padding}"
        )
        // calculate the sunrise and sunset times
        val solarTimes = timeCalculator.solarTimeForLocation(prefs.location)
        ctx.times = timeCalculator.getTimesForDate(solarTimes, PlainDate.nowInSystemTime())
        return ctx
    }

    private fun updateCircle(ctx: WidgetContext, views: RemoteViews) {
        val diameter = (2 * (ctx.radius + ctx.padding))
        val circleBox = RectF(ctx.padding, ctx.padding, diameter-ctx.padding, diameter-ctx.padding)
        val bitmap = Bitmap.createBitmap(diameter.toInt(), diameter.toInt(), Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        val strokeWidth = ctx.padding / 1.5F
        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        basePaint.style = Paint.Style.STROKE
        basePaint.strokeWidth = strokeWidth
        basePaint.strokeCap = Paint.Cap.ROUND

        val daylightPaint = Paint(basePaint)
        setColor(daylightPaint, ctx.prefs.daylightColor)
        val sunrisePaint = Paint(basePaint)
        setColor(sunrisePaint, ctx.prefs.sunriseColor)
        val sunsetPaint = Paint(basePaint)
        setColor(sunsetPaint, ctx.prefs.sunsetColor)
        val nightPaint = Paint(basePaint)
        setColor(nightPaint, ctx.prefs.nightColor)

        // background
        val backgroundPaint = Paint(basePaint)
        setColor(backgroundPaint, ctx.prefs.backgroundColor)
        backgroundPaint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawCircle((diameter)/2, (diameter)/2, ctx.radius-strokeWidth, backgroundPaint)

        // the circle
        val sunriseDeg = toDeg(ctx.times.sunrise)
        val sunsetDeg = toDeg(ctx.times.sunset)
        val sunset2Deg = toDeg(ctx.times.sunsetTwilight)
        val sunrise2Deg = toDeg(ctx.times.sunriseTwilight)

        canvas.drawArc(circleBox, sunriseDeg, dist(sunriseDeg, sunsetDeg), false, daylightPaint)
        canvas.drawArc(circleBox, sunset2Deg, dist(sunset2Deg, sunrise2Deg), false, nightPaint)
        canvas.drawArc(circleBox, sunsetDeg, dist(sunsetDeg, sunset2Deg), false, sunsetPaint)
        canvas.drawArc(circleBox, sunrise2Deg, dist(sunrise2Deg, sunriseDeg), false, sunrisePaint)

        // draw the marker on the circle
        val markerDeg = toDeg(Moment.nowInSystemTime())
        val markerRad = markerDeg * PI / 180.0
        val xPosition = (ctx.radius * cos(markerRad) + diameter/2).toFloat()
        val yPosition = (ctx.radius * sin(markerRad) + diameter/2).toFloat()

        val markerPaint = Paint(basePaint)
        setColor(markerPaint, ctx.prefs.markerColor)
        markerPaint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawCircle(xPosition, yPosition, ctx.padding / 1.5F, markerPaint)

        views.setImageViewBitmap(R.id.clock_widget_background, bitmap)
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

    private fun setColor(paint: Paint, @ColorInt colorNum: Int) {
        val hexColor = java.lang.String.format("#%08X", 0xFFFFFFFF.toInt() and colorNum)
        paint.color = Color.parseColor(hexColor)
    }
    private fun dist(degFrom: Float, degTo: Float): Float {
        return if (degFrom <= degTo) {
            degTo - degFrom
        } else {
            360 - (degFrom - degTo)
        }
    }


    private fun updateClock(ctx: WidgetContext, views: RemoteViews) {
        val timeFormat =
            (if(ctx.prefs.use24Hours) "H" else "h") +
            ":mm" +
            (if(ctx.prefs.showSeconds) ":ss" else "") +
            (if(ctx.prefs.use24Hours) "" else " a")

        views.setCharSequence(R.id.clock_widget_clock, "setFormat24Hour", timeFormat)
        views.setCharSequence(R.id.clock_widget_clock, "setFormat12Hour", timeFormat)

        val clockFontSize = getFontSize(ctx.radius, ctx.padding, timeFormat, ctx.prefs)
        Log.i(TAG, "setting fontsize: $clockFontSize")
        views.setFloat(R.id.clock_widget_clock, "setTextSize", clockFontSize)

        views.setInt(R.id.clock_widget_clock, "setTextColor", ctx.prefs.clockColor)
    }

    private fun getFontSize(
        radius: Float,
        padding: Float,
        timeFormat: String,
        prefs: WidgetPreferences
    ): Float {
        val currentTime = SimpleDateFormat(timeFormat).format(Date())
        val maxWidth = 2 * (radius)
        // count chars (but : and . only as have width)
        val charCount = currentTime.length - currentTime.filter { setOf(':','.').contains(it) }.count() * 0.5
        Log.i(TAG, "maxWidth for Text: $maxWidth, per char: ${maxWidth / charCount}")
        return ((maxWidth / charCount * 0.6)).toFloat()
    }

}