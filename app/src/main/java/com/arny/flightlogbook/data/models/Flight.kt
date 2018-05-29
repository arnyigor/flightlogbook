package com.arny.flightlogbook.data.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.arny.arnylib.utils.Utility
import org.chalup.microorm.annotations.Column

@Entity(tableName = "main_table")
data class Flight(@PrimaryKey()
                  @ColumnInfo(name = "_id")
                  var id: Long = 0) {
    constructor() : this(0)
    var date: String? = null
    var datetime: Long = 0
    @ColumnInfo(name = "log_time")
    var logtime: Int = 0
    var reg_no: String? = null
    @ColumnInfo(name = "airplane_type_title")
    var airplanetypetitle: String? = null
    @ColumnInfo(name = "airplane_type")
    var airplanetypeid: Int = 0
    @ColumnInfo(name = "day_night")
    var daynight: Int = 0
    @ColumnInfo(name = "ifr_vfr")
    var ifrvfr: Int = 0
    @ColumnInfo(name = "flight_type")
    var flighttype: Int = 0
    var description: String? = null

    override fun toString(): String {
        return Utility.getFields(this)
    }
}
