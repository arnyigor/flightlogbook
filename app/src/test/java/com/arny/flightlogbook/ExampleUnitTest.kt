package com.arny.flightlogbook

import android.content.Intent
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.junit.MockitoJUnitRunner
import com.arny.arnylib.utils.DateTimeUtils.getTimeDiff
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime


@RunWith(MockitoJUnitRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ExampleUnitTest {

    @Test
    @Throws(Exception::class)
    fun az_getTimeDiffT() {
        val now = DateTime.now()
        val plusMinutes = now.plusMinutes(10)
        val h = plusMinutes.hourOfDay
        val min = plusMinutes.minuteOfHour
        val timeDiff = getTimeDiff(0)
        assertThat(timeDiff).isGreaterThan(0.0)
    }

    @Test
    @Throws(Exception::class)
    fun aa_getIntentExtra() {
    }
}