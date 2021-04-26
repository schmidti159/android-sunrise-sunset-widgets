import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.BatteryManager
import android.text.TextPaint
import android.text.format.DateFormat
import android.text.style.TypefaceSpan
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
        updateDate(ctx, views)
        updateBattery(ctx, views)

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
            WidgetPreferenceProvider.updatePreferences(
                WidgetPreferences.DEFAULT_PREFS,
                androidContext
            )
        }
        val prefs = WidgetPreferenceProvider.getPreferences(widgetId, androidContext)
            ?: throw IllegalStateException("Preferences for $widgetId are still not initialized after updating them")

        var size = WidgetSizeProvider.getSize(widgetId, androidContext)
        if(size == null) {
            Log.w(
                TAG,
                "Sizes for $widgetId are not initialized. Reading sizes from the widgetManager."
            )
            val widgetOptions = AppWidgetManager.getInstance(androidContext).getAppWidgetOptions(
                widgetId
            )
            WidgetSizeProvider.updateSize(widgetId, widgetOptions, androidContext)
            size = WidgetSizeProvider.getSize(widgetId, androidContext)
                ?: throw IllegalStateException("Sizes for $widgetId are still not initialized after updating them")
        }

        val ctx = WidgetContext(prefs, size, androidContext)
        // calculate the radius and padding
        val width = size.getWidth(androidContext.resources) * androidContext.resources.displayMetrics.density
        val height = size.getHeight(androidContext.resources) * androidContext.resources.displayMetrics.density
        val maxRadius = min(width, height) / 2
        ctx.padding = min(10F, maxRadius / 20)
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
        val circleBox = RectF(
            ctx.padding,
            ctx.padding,
            diameter - ctx.padding,
            diameter - ctx.padding
        )
        val bitmap = Bitmap.createBitmap(
            diameter.toInt(),
            diameter.toInt(),
            Bitmap.Config.ARGB_8888
        )

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
        canvas.drawCircle((diameter) / 2, (diameter) / 2, ctx.radius - strokeWidth, backgroundPaint)

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

        val currentTime = SimpleDateFormat(timeFormat).format(Date())
        val clockFontSize = getFontSize(ctx, currentTime)
        Log.i(TAG, "setting fontsize: $clockFontSize for clock")
        views.setFloat(R.id.clock_widget_clock, "setTextSize", clockFontSize)

        views.setInt(R.id.clock_widget_clock, "setTextColor", ctx.prefs.clockColor)
    }

    private fun getFontSize(ctx: WidgetContext, sampleString: String): Float {
        val maxWidth = 2 * (ctx.radius)
        // count chars (but : and . only as have width)
        val charCount = sampleString.length - sampleString.filter { setOf(':', '.', '/', '\\', ' ').contains(
            it
        ) }.count() * 0.5
        Log.d(TAG, "maxWidth for Text: $maxWidth, per char: ${maxWidth / charCount}")
        return ((maxWidth / charCount * 0.8)).toFloat()
    }

    private fun updateDate(ctx: WidgetContext, views: RemoteViews) {
        if(!ctx.prefs.showDate) {
            views.setCharSequence(R.id.clock_widget_date, "setText", "")
            return
        }
        val dateFormat = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEdMMM")
        val date = SimpleDateFormat(dateFormat).format(Date())
        Log.i(TAG, "current date: $date")
        views.setCharSequence(R.id.clock_widget_date, "setText", date)
        val fontSize = getFontSize(ctx, date) * 0.85F;
        val bottomPadding = (ctx.radius / 1.1).toInt()
        Log.i(
            TAG,
            "setting fontsize: $fontSize and bottomPadding $bottomPadding for date with value $date"
        )
        views.setFloat(R.id.clock_widget_date, "setTextSize", fontSize)
        views.setViewPadding(R.id.clock_widget_date, 0, 0, 0, bottomPadding)
        views.setInt(R.id.clock_widget_date, "setTextColor", ctx.prefs.dateColor)

    }

    private fun updateBattery(ctx: WidgetContext, views: RemoteViews) {
        if(!ctx.prefs.showBattery) {
            views.setCharSequence(R.id.clock_widget_battery, "setText", "")
            return
        }
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            ctx.androidCtx.registerReceiver(null, ifilter)
        }
        val batteryPct = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            (level * 100 / scale.toFloat()).toInt()
        }
        val batteryString = "$batteryPct%"
        views.setTextViewText(R.id.clock_widget_battery, batteryString)
        val fontSize = getFontSize(ctx, batteryString.toString()) * 0.85F / 3;
        val topPadding = (ctx.radius / 1).toInt()
        Log.i(
            TAG,
            "setting fontsize: $fontSize and topPadding $topPadding for battery with value $batteryString"
        )
        views.setFloat(R.id.clock_widget_battery, "setTextSize", fontSize)
        views.setViewPadding(R.id.clock_widget_battery, 0, topPadding, 0, 0)
        views.setInt(R.id.clock_widget_battery, "setTextColor", ctx.prefs.dateColor)

//      calendar: uf073, battery empty f244, quarter f243, half f242, three-quarters f241, full f240

    }

    class CustomTypefaceSpan(private val customTypeface: Typeface) : TypefaceSpan("") {
        override fun updateDrawState(ds: TextPaint) {
            applyCustomTypeFace(ds, customTypeface)
        }

        override fun updateMeasureState(paint: TextPaint) {
            applyCustomTypeFace(paint, customTypeface)
        }
        private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
            val oldStyle: Int
            val oldTypeFace = paint.typeface
            oldStyle = oldTypeFace.style
            val fake = oldStyle and tf.style.inv()
            if (fake and Typeface.BOLD != 0) {
                paint.isFakeBoldText = true
            }
            if (fake and Typeface.ITALIC != 0) {
                paint.textSkewX = -0.25f
            }
            paint.typeface = tf
        }
    }

}