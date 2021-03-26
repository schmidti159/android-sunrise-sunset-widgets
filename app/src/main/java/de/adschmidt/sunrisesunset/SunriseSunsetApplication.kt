package de.adschmidt.sunrisesunset

import android.app.Application
import android.util.Log
import net.time4j.android.ApplicationStarter

class SunriseSunsetApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ApplicationStarter.initialize(this, true) // with prefetch on background thread
        Log.i(TAG, "initialized Application")

    }
}

val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }