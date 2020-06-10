package com.arny.flightlogbook.presentation.statistic.presenter

sealed class PeriodType(val canShowDialog: Boolean, val showCustomRange: Boolean) {
    object ALL : PeriodType(false, false)
    object DAY : PeriodType(true, false)
    object MONTH : PeriodType(true, false)
    object YEAR : PeriodType(true, false)
    object CUSTOM : PeriodType(false, true)
}