package com.arny.flightlogbook.common

fun getFilterflights(filtertype:Int):String = when(filtertype){
    0->Local.COLUMN_DATETIME + " ASC"
    1->Local.COLUMN_DATETIME + " DESC"
    3->Local.COLUMN_LOG_TIME + " ASC"
    2->Local.COLUMN_LOG_TIME + " DESC"
    else->Local.COLUMN_DATETIME + " ASC"
}