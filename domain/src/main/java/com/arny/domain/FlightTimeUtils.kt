package com.arny.domain

import com.arny.helpers.utils.DateTimeUtils
import com.arny.helpers.utils.parseInt

/**
 *Created by Sedoy on 23.07.2019
 */

suspend fun correctLogTime(stringTime: String, initTime: Int): Pair<Int, String> {
    var logMinutes: Int
    var logHours = 0
    var logTime = initTime
    return when {
        stringTime.isBlank() -> Pair(logTime, if (logTime != 0) DateTimeUtils.strLogTime(logTime) else "")
        stringTime.length == 1 -> {
            logTime = stringTime.parseInt() ?: 0
            Pair(logTime, if (logTime != 0) String.format("00:0%d", logTime) else "")
        }
        stringTime.length == 2 -> {
            logMinutes = stringTime.parseInt() ?: 0
            logTime = stringTime.parseInt() ?: 0
            if (logMinutes > 59) {
                logHours = 1
                logMinutes -= 60
            }
            val format = String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
            Pair(logTime, format)
        }
        stringTime.length > 2 -> {
            if (stringTime.contains(":")) {
                logMinutes = stringTime.substring(stringTime.length - 2, stringTime.length).parseInt()
                        ?: 0
                logHours = stringTime.substring(0, stringTime.length - 3).parseInt() ?: 0
            } else {
                logMinutes = stringTime.substring(stringTime.length - 2, stringTime.length).parseInt()
                        ?: 0
                logHours = stringTime.substring(0, stringTime.length - 2).parseInt() ?: 0
            }
            if (logMinutes > 59) {
                logHours += 1
                logMinutes -= 60
            }
            logTime = DateTimeUtils.logTimeMinutes(logHours, logMinutes)
            Pair(logTime, DateTimeUtils.strLogTime(logTime))
        }
        else -> Pair(logTime, if (logTime != 0) DateTimeUtils.strLogTime(logTime) else "")
    }
}