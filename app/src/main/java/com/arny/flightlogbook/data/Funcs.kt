package com.arny.flightlogbook.data

fun getFilterflights(filtertype:Int):String = when(filtertype){
    0-> Consts.DB.COLUMN_DATETIME + " ASC"
    1-> Consts.DB.COLUMN_DATETIME + " DESC"
    3-> Consts.DB.COLUMN_LOG_TIME + " DESC"
    2-> Consts.DB.COLUMN_LOG_TIME + " ASC"
    else-> Consts.DB.COLUMN_DATETIME + " ASC"
}