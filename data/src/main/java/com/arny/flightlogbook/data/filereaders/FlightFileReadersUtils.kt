package com.arny.flightlogbook.data.filereaders

import com.arny.core.utils.parseDouble
import com.arny.core.utils.parseLong

fun getFlightTypeId(fTypeStr: String): Long =
    if (fTypeStr.contains(".")) {
        fTypeStr.parseDouble()?.toLong() ?: -1
    } else {
        fTypeStr.parseLong() ?: -1
    }