package com.arny.flightlogbook.presentation.flights.addedit.models

import com.arny.core.utils.DateTimeUtils
import com.arny.core.utils.parseInt

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

private fun correctMinHours(hours: Int, mins: Int): Pair<Int, Int> {
    var logHours = hours
    var logMinutes = mins
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
            val (hours, minutes) = correctMinHours(logHours, logMinutes)
            CorrectedTimePair(
                intTime = logTime,
                strTime = String.format(
                    "%s:%s",
                    DateTimeUtils.pad(hours),
                    DateTimeUtils.pad(minutes)
                )
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

fun getCorrectLocalDiffDayTime(stringTime: String, initTime: Int): CorrectedTimePair {
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
                strTime = if (logTime != 0) String.format("00:0%d", logTime) else "",
                sign = 1
            )
        }
        stringTime.length == 2 -> {
            when {
                stringTime.contains("-") -> { //-9->-00:09
                    sign = if (stringTime.getOrNull(0) == '-') -1 else 1
                    logMinutes =
                        stringTime.substring(stringTime.length - 2, stringTime.length).parseInt(0)
                    logHours = stringTime.substring(1, stringTime.length - 2).parseInt(0)
                }
                else -> {
                    logMinutes =
                        stringTime.substring(stringTime.length - 2, stringTime.length).parseInt(0)
                    logHours = stringTime.substring(0, stringTime.length - 2).parseInt(0)
                }
            }
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
            when {
                stringTime == "00:00" -> {
                    logMinutes = 0
                    logHours = 0
                }
                stringTime.contains(":") -> {
                    logMinutes =
                        stringTime.substring(stringTime.length - 2, stringTime.length).parseInt(0)
                    logHours = stringTime.substring(0, stringTime.length - 3).parseInt(0)
                }
                else -> {
                    logMinutes =
                        stringTime.substring(stringTime.length - 2, stringTime.length).parseInt(0)
                    logHours = stringTime.substring(0, stringTime.length - 2).parseInt(0)
                }
            }
            val (hours, minutes) = correctMinHours(logHours, logMinutes)
            logTime = DateTimeUtils.logTimeMinutes(hours, minutes)
            CorrectedTimePair(
                intTime = logTime,
                strTime = DateTimeUtils.strLogTime(logTime)
            )
        }
        else -> CorrectedTimePair(
            intTime = logTime,
            strTime = if (logTime != 0) DateTimeUtils.strLogTime(logTime) else ""
        )
    }
}
