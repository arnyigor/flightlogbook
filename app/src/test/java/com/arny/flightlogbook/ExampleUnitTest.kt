package com.arny.flightlogbook

import com.arny.core.utils.DateTimeUtils
import com.arny.core.utils.Utility.getTimeDiff
import junit.framework.Assert.assertTrue
import org.joda.time.DateTime
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ExampleUnitTest {

    @Test
    @Throws(Exception::class)
    fun aa_getTimeDiffT() {
        val now = DateTime.now()
        val plusMinutes = now.plusMinutes(10)
        val h = plusMinutes.hourOfDay
        val min = plusMinutes.minuteOfHour
        val timeDiff = getTimeDiff(0)
//        assertThat(timeDiff).isGreaterThan(0.0)
    }

    @Test
    @Throws(Exception::class)
    fun ab_convertStringToTime() {
        val time = DateTimeUtils.convertStringToTime("00:05")
        assert(time == 5)
    }

    @Test
    @Throws(Exception::class)
    fun ac_convertStringIntsToTime() {
        val time = DateTimeUtils.convertStringToTime("0005")
        assertTrue(time == 5)
    }

    @Test
    @Throws(Exception::class)
    fun ad_convertStringIntsMoreToTime() {
        val time = DateTimeUtils.convertStringToTime("75")
        assertTrue(time == 75)
    }

    @Test
    @Throws(Exception::class)
    fun af_convertStringIntsMoreToTime() {
        val time = DateTimeUtils.convertStringToTime("01:05")
        assertTrue(time == 65)
    }

    @Test
    @Throws(Exception::class)
    fun ba_convertStringIntsMoreToTime() {
        val time = DateTimeUtils.convertStringToTime("gg")
        assertTrue(time == 0)
    }

    @Test
    @Throws(Exception::class)
    fun bb_convertStringIntsMoreToTime() {
        val time = DateTimeUtils.convertStringToTime("null")
        assertTrue(time == 0)
    }
}
