package com.arny.flightlogbook.data.models.flights

import com.arny.flightlogbook.domain.models.Flight
import org.junit.Assert.assertEquals
import org.junit.Test

internal class FlightEntityTest {
    companion object{
        const val ONE_PARAM_STRING = "{color=#009BFF}"
    }
    @Test
    fun flightParamsConvertToString() {
        val flight = Flight()
        flight.customParams = hashMapOf()
        flight.customParams?.set("color", "#009BFF")
        val stringParams = flight.customParams?.fromParams()
        assertEquals(
            ONE_PARAM_STRING,
            stringParams
        )
    }
    @Test
    fun flightParamsStringConvertToParams() {
        val flight = Flight()
        flight.customParams = hashMapOf()
        flight.customParams?.set("color", "#009BFF")

        val params = ONE_PARAM_STRING.toParams()

        assertEquals(
            params,
            flight.customParams
        )
    }
}