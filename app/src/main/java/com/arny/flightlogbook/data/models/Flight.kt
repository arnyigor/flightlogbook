package com.arny.flightlogbook.data.models

import com.arny.arnylib.utils.Utility
import org.chalup.microorm.annotations.Column

class Flight {
    @Column("_id")
    var id: Int = 0
    @Column("datetime")
    var datetime: Long = 0
    @Column("log_time")
    var logtime: Int = 0
    @Column("reg_no")
    var reg_no: String? = null
    @Column("airplane_type_title")
    var airplanetypetitle: String? = null
    @Column("airplane_type")
    var airplanetypeid: Int = 0
    @Column("day_night")
    var daynight: Int = 0
    @Column("ifr_vfr")
    var ifrvfr: Int = 0
    @Column("flight_type")
    var flighttype: Int = 0
    @Column("description")
    var description: String? = null

    override fun toString(): String {
        return Utility.getFields(this)
    }
}
