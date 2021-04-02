package de.adschmidt.sunrisesunset.model

import net.time4j.Moment

data class Times(
    val sunriseTwilight: Moment = Moment.nowInSystemTime(),
    val sunrise: Moment = Moment.nowInSystemTime(),
    val sunset: Moment = Moment.nowInSystemTime(),
    val sunsetTwilight: Moment = Moment.nowInSystemTime()
)