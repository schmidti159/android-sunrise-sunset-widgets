package de.adschmidt.sunrisesunset.calc

import android.location.Location
import de.adschmidt.sunrisesunset.model.Times
import net.time4j.PlainDate
import net.time4j.calendar.astro.SolarTime
import net.time4j.calendar.astro.StdSolarCalculator
import net.time4j.calendar.astro.Twilight
import kotlin.math.abs
import kotlin.math.floor

class TimeCalculator {
    fun solarTimeForLocation(location: Location): SolarTime {
        val solarTimeBuilder = SolarTime.ofLocation()
            .atAltitude(location.altitude.toInt())
            .usingCalculator(StdSolarCalculator.TIME4J)
        if (location.latitude >= 0) {
            solarTimeBuilder.northernLatitude(
                getDegrees(location.latitude),
                getMinutes(location.latitude),
                getSeconds(location.latitude)
            )
        } else {
            solarTimeBuilder.southernLatitude(
                getDegrees(location.latitude),
                getMinutes(location.latitude),
                getSeconds(location.latitude)
            )
        }
        if (location.longitude >= 0) {
            solarTimeBuilder.easternLongitude(
                getDegrees(location.longitude),
                getMinutes(location.longitude),
                getSeconds(location.longitude)
            )
        } else {
            solarTimeBuilder.westernLongitude(
                getDegrees(location.longitude),
                getMinutes(location.longitude),
                getSeconds(location.longitude)
            )
        }
        return solarTimeBuilder.build()
    }

    fun getTimesForDate(solarTime: SolarTime, date: PlainDate): Times {
        return Times(
            date.get(solarTime.sunrise(Twilight.CIVIL)),
            date.get(solarTime.sunrise()),
            date.get(solarTime.sunset()),
            date.get(solarTime.sunset(Twilight.CIVIL))
        )
    }

    private fun getDegrees(degrees: Double): Int {
        return floor(abs(degrees)).toInt()
    }

    private fun getMinutes(degrees: Double): Int {
        val minutesInDegrees = abs(degrees) - getDegrees(degrees)
        return floor(minutesInDegrees * 60).toInt()
    }

    private fun getSeconds(degrees: Double): Double {
        val secondsInDegrees = abs(degrees) - getDegrees(degrees) - getMinutes(degrees) / 60.0
        return floor(secondsInDegrees * 60 * 60)

    }

}