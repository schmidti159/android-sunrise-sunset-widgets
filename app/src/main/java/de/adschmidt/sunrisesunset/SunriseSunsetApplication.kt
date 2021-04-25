package de.adschmidt.sunrisesunset

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import net.time4j.android.ApplicationStarter

class SunriseSunsetApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ApplicationStarter.initialize(this, true) // with prefetch on background thread
        Log.i(TAG, "initialized Application")

    }
}

// property TAG on any class will always represent the current class name
val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

// store double as longs in shared preferences
fun SharedPreferences.Editor.putDouble(key: String, double: Double) =
    putLong(key, java.lang.Double.doubleToRawLongBits(double))

fun SharedPreferences.getDouble(key: String, default: Double) =
    java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))