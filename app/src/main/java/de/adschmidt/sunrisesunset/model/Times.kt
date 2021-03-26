package de.adschmidt.sunrisesunset.model

import net.time4j.Moment

data class Times(
    val sunriseTwilight: Moment,
    val sunrise: Moment,
    val sunset: Moment,
    val sunsetTwilight: Moment
)