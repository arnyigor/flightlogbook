package com.arny.domain

import com.arny.helpers.utils.DateTimeUtils
import com.arny.helpers.utils.parseInt

/**
 *Created by Sedoy on 23.07.2019
 */

fun correctLogTime(stringTime: String, initTime: Int, onCorrect: (logTime: Int, timeText: String) -> Unit) {
    var logMinutes: Int
    var logHours = 0
    var logTime = initTime
    when {
        stringTime.isBlank() -> {
            val text = if (logTime != 0) DateTimeUtils.strLogTime(logTime) else ""
            onCorrect.invoke(logTime, text)
        }
        stringTime.length == 1 -> {
            logTime = stringTime.parseInt() ?: 0
            val text = if (logTime != 0) String.format("00:0%d", logTime) else ""
            onCorrect.invoke(logTime, text)
        }
        stringTime.length == 2 -> {
            logMinutes = stringTime.parseInt() ?: 0
            logTime = stringTime.parseInt() ?: 0
            if (logMinutes > 59) {
                logHours = 1
                logMinutes -= 60
            }
            val format = String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
            onCorrect.invoke(logTime, format)
        }
        stringTime.length > 2 -> {
            if (stringTime.contains(":")) {
                logMinutes = stringTime.substring(stringTime.length - 2, stringTime.length).parseInt() ?: 0
                logHours = stringTime.substring(0, stringTime.length - 3).parseInt() ?: 0
            } else {
                logMinutes = stringTime.substring(stringTime.length - 2, stringTime.length).parseInt() ?: 0
                logHours = stringTime.substring(0, stringTime.length - 2).parseInt() ?: 0
            }
            if (logMinutes > 59) {
                logHours += 1
                logMinutes -= 60
            }
            logTime = DateTimeUtils.logTimeMinutes(logHours, logMinutes)
            onCorrect.invoke(logTime, DateTimeUtils.strLogTime(logTime))
        }
    }
}