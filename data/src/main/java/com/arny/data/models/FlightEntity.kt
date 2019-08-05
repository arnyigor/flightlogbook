package com.arny.data.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "main_table")
data class FlightEntity(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var id: Long? = null) {
    var date: String? = null
    var datetime: Long? = null
    @ColumnInfo(name = "log_time")
    var logtime: Int? = null
    var reg_no: String? = null
    @Ignore
    var airplanetypetitle: String? = null
    @ColumnInfo(name = "airplane_type")
    var aircraft_id: Long? = null
    @ColumnInfo(name = "day_night")
    var daynight: Int? = null
    @ColumnInfo(name = "ifr_vfr")
    var ifrvfr: Int? = null
    @ColumnInfo(name = "flight_type")
    var flighttype: Int? = null
    var description: String? = null


}
