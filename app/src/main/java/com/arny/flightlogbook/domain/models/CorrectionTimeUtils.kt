package com.arny.flightlogbook.domain.models

import com.arny.flightlogbook.data.utils.DateTimeUtils
import com.arny.flightlogbook.presentation.utils.parseInt
import java.util.Locale

private fun correctMinHours(hours: Int, minutes: Int): Pair<Int, Int> {
    var logHours = hours
    var logMinutes = minutes
    while (true) {
        if (logMinutes > 59) {
            logHours += 1
            logMinutes -= 60
        } else {
            break
        }
    }
    while (true) {
        if (logHours > 23) {
            logHours -= 24
        } else {
            break
        }
    }
    return Pair(logHours, logMinutes)
}

fun getCorrectDayTime(stringTime: String, initTime: Int): CorrectedTimePair {
    val logMinutes: Int
    val logHours: Int
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
                if (logTime != 0) String.format(Locale.getDefault(), "00:0%d", logTime) else ""
            )
        }

        stringTime.length >= 2 -> {
            if (stringTime.contains(":")) {
                logHours = stringTime.substringBefore(":").toIntOrNull() ?: 0
                logMinutes = stringTime.substringAfter(":").toIntOrNull() ?: 0
            } else {
                logMinutes = stringTime.toIntOrNull() ?: 0
                logHours = 0
            }
            val (hours, minutes) = correctMinHours(logHours, logMinutes)
            logTime = DateTimeUtils.logTimeMinutes(hours, minutes)
            CorrectedTimePair(
                intTime = logTime,
                strTime = DateTimeUtils.strLogTime(logTime)
            )
        }

        else -> CorrectedTimePair(
            logTime,
            if (logTime != 0) DateTimeUtils.strLogTime(logTime) else ""
        )
    }
}

fun getCorrectLocalDiffDayTime(inputTime: String, initTime: Int): CorrectedTimePair {
    var stringTime = inputTime
    val logMinutes: Int
    val logHours: Int
    var logTime = initTime
    var sign = 1
    return when {
        stringTime.isBlank() -> CorrectedTimePair(
            intTime = logTime,
            strTime = if (logTime != 0) DateTimeUtils.strLogTime(logTime) else ""
        )

        stringTime.length == 1 -> {
            logTime = if (stringTime.contains("-")) 0 else stringTime.parseInt(0)
            CorrectedTimePair(
                intTime = logTime,
                strTime = if (logTime != 0) String.format(
                    Locale.getDefault(),
                    "00:0%d",
                    logTime
                ) else "",
                sign = 1
            )
        }

        stringTime.length == 2 -> {
            if (stringTime.contains("-")) {
                sign = if (stringTime.getOrNull(0) == '-') -1 else 1
                stringTime = stringTime.replace("-", "")
            }
            logTime = stringTime.toIntOrNull() ?: 0
            logMinutes = logTime % 60
            logHours = logTime / 60
            val (hours, minutes) = correctMinHours(logHours, logMinutes)
            CorrectedTimePair(
                intTime = logTime,
                strTime = String.format(
                    "%s:%s",
                    DateTimeUtils.pad(hours),
                    DateTimeUtils.pad(minutes)
                ),
                sign = sign
            )
        }

        stringTime.length > 2 -> {
            if (stringTime.contains("-")) {
                sign = if (stringTime.getOrNull(0) == '-') -1 else 1
                stringTime = stringTime.replace("-", "")
            }
            when {
                stringTime == "00:00" -> {
                    logMinutes = 0
                    logHours = 0
                }

                stringTime.contains(":") -> {
                    logHours = stringTime.substringBefore(":").toIntOrNull() ?: 0
                    logMinutes = stringTime.substringAfter(":").toIntOrNull() ?: 0
                }

                else -> {
                    val minutes = stringTime.toIntOrNull() ?: 0
                    logMinutes = minutes % 60
                    logHours = minutes / 60
                }
            }
            val (hours, minutes) = correctMinHours(logHours, logMinutes)
            logTime = DateTimeUtils.logTimeMinutes(hours, minutes)
            CorrectedTimePair(
                intTime = logTime,
                strTime = DateTimeUtils.strLogTime(logTime),
                sign = sign
            )
        }

        else -> CorrectedTimePair(
            intTime = logTime,
            strTime = if (logTime != 0) DateTimeUtils.strLogTime(logTime) else ""
        )
    }
}
