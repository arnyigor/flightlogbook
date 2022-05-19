package com.arny.flightlogbook.data.models.flights

import com.arny.flightlogbook.domain.models.Flight
import org.junit.Assert.assertEquals
import org.junit.Test

internal class FlightEntityTest {
    private companion object {
        const val COLOR_PARAM_KEY = "color"
        const val COLOR2_PARAM_KEY = "color2"
        const val COLOR_PARAM_VALUE = "#009BFF"
        const val COLOR2_PARAM_VALUE = "#efaf8c"
        const val COLOR_PARAM_STRING = "{color=#009BFF}"
        const val COLOR_FULL_PARAM_STRING = "{color2=#efaf8c,color=#009BFF}"
    }

    @Test
    fun flightParamsConvertToString() {
        val flight = Flight()
        flight.customParams = hashMapOf()
        flight.customParams?.set(COLOR_PARAM_KEY, COLOR_PARAM_VALUE)
        val stringParams = flight.customParams?.fromParams()

        assertEquals(
            COLOR_PARAM_STRING,
            stringParams
        )
    }

    @Test
    fun flightParamsStringConvertToParams() {
        val flight = Flight()
        flight.customParams = hashMapOf()
        flight.customParams?.set(COLOR_PARAM_KEY, COLOR_PARAM_VALUE)
        val params = COLOR_PARAM_STRING.toParams()

        assertEquals(
            flight.customParams?.get(COLOR_PARAM_KEY),
            params?.get(COLOR_PARAM_KEY)
        )
    }

    @Test
    fun flightParamsFullConvertToString() {
        val flight = Flight()
        flight.customParams = hashMapOf()
        flight.customParams?.set(COLOR_PARAM_KEY, COLOR_PARAM_VALUE)
        flight.customParams?.set(COLOR2_PARAM_KEY, COLOR2_PARAM_VALUE)
        val stringParams = flight.customParams?.fromParams()

        assertEquals(
            COLOR_FULL_PARAM_STRING,
            stringParams
        )
    }

    @Test
    fun flightParamsFullStringConvertToParams() {
        val flight = Flight()
        flight.customParams = hashMapOf()
        flight.customParams?.set(COLOR_PARAM_KEY, COLOR_PARAM_VALUE)
        flight.customParams?.set(COLOR2_PARAM_KEY, COLOR2_PARAM_VALUE)
        val params = COLOR_FULL_PARAM_STRING.toParams()

        assertEquals(
            flight.customParams?.get(COLOR2_PARAM_KEY),
            params?.get(COLOR2_PARAM_KEY)
        )
    }
}