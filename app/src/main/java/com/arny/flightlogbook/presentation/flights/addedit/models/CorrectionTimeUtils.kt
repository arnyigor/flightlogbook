package com.arny.flightlogbook.presentation.flights.addedit.models

import com.arny.helpers.utils.DateTimeUtils
import com.arny.helpers.utils.parseInt

fun getCorrectTime(stringTime: String, initTime: Int): CorrectedTimePair {
    var logMinutes: Int
    var logHours = 0
    var logTime = initTime
    return when {
        stringTime.isBlank() -> CorrectedTimePair(
                logTime,
                if (logTime != 0) DateTimeUtils.strLogTime(logTime) else ""
        )
        stringTime.length == 1 -> {
            logTime = stringTime.parseInt(0)
            CorrectedTimePair(
                    logTime,
                    if (logTime != 0) String.format("00:0%d", logTime) else ""
            )
        }
        stringTime.length == 2 -> {
            logMinutes = stringTime.parseInt(0)
            logTime = stringTime.parseInt(0)
            if (logMinutes > 59) {
                logHours = 1
                logMinutes -= 60
            }
            val format = String.format(
                    "%s:%s",
                    DateTimeUtils.pad(logHours),
                    DateTimeUtils.pad(logMinutes)
            )
            CorrectedTimePair(
                    logTime,
                    format
            )
        }
        stringTime.length > 2 -> {
            if (stringTime.contains(":")) {
                logMinutes =
                        stringTime.substring(stringTime.length - 2, stringTime.length).parseInt(0)
                logHours = stringTime.substring(0, stringTime.length - 3).parseInt(0)
            } else {
                logMinutes =
                        stringTime.substring(stringTime.length - 2, stringTime.length).parseInt(0)
                logHours = stringTime.substring(0, stringTime.length - 2).parseInt(0)
            }
            if (logMinutes > 59) {
                logHours += 1
                logMinutes -= 60
            }
            logTime = DateTimeUtils.logTimeMinutes(logHours, logMinutes)
            CorrectedTimePair(
                    logTime,
                    DateTimeUtils.strLogTime(logTime)
            )
        }
        else -> CorrectedTimePair(
                logTime,
                if (logTime != 0) DateTimeUtils.strLogTime(logTime) else ""
        )
    }
}
