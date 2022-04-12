package com.arny.flightlogbook.data.models.flights

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arny.core.utils.DateTimeUtils
import com.arny.flightlogbook.domain.models.Flight
import com.arny.flightlogbook.domain.models.Params

@Entity(tableName = "main_table")
data class FlightEntity constructor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long? = null,
    var date: String? = null,
    var datetime: Long? = null,
    @ColumnInfo(name = "log_time")
    var logtime: Int? = null,
    @ColumnInfo(name = "ground_time")
    var groundTime: Int? = null,
    @ColumnInfo(name = "night_time")
    var nightTime: Int? = null,
    @ColumnInfo(name = "reg_no")
    var regNo: String? = null,
    @ColumnInfo(name = "airplane_type")
    var planeId: Long? = null,
    @ColumnInfo(name = "day_night")
    var daynight: Int? = null,
    @ColumnInfo(name = "ifr_vfr")
    var ifrvfr: Int? = null,
    @ColumnInfo(name = "flight_type")
    var flighttype: Long? = null,
    var description: String? = null,
    var params: String? = null,
    @ColumnInfo(name = "departure_id")
    var departureId: Long? = null,
    @ColumnInfo(name = "arrival_id")
    var arrivalId: Long? = null,
    @ColumnInfo(name = "departure_utc_time")
    var departureUtcTime: String? = null,
    @ColumnInfo(name = "arrival_utc_time")
    var arrivalUtcTime: String? = null,
) {

    fun toFlight(): Flight {
        val flight = Flight(id)
        flight.datetime = datetime
        flight.datetimeFormatted =
            this.datetime?.let { DateTimeUtils.getDateTime(it, "dd MMM yyyy") }
        flight.flightTime = logtime ?: 0
        flight.logtimeFormatted = logtime?.let { DateTimeUtils.strLogTime(it) }
        flight.regNo = regNo
        flight.totalTime = (logtime ?: 0) + (this.groundTime ?: 0)
        flight.nightTime = nightTime ?: 0
        flight.groundTime = groundTime ?: 0
        flight.planeId = planeId
        flight.daynight = daynight
        flight.ifrvfr = ifrvfr
        flight.flightTypeId = flighttype
        flight.description = description
        flight.params = Params(params)
        flight.departureId = departureId
        flight.arrivalId = arrivalId
        flight.departureUtcTime = DateTimeUtils.convertStringToTime(departureUtcTime)
        flight.arrivalUtcTime = DateTimeUtils.convertStringToTime(arrivalUtcTime)
        return flight
    }
}

fun Flight.toFlightEntity(): FlightEntity =
    FlightEntity(
        id = id,
        date = null,
        datetime = datetime,
        logtime = flightTime,
        groundTime = groundTime,
        nightTime = nightTime,
        regNo = regNo,
        planeId = planeId,
        daynight = daynight,
        ifrvfr = ifrvfr,
        flighttype = flightTypeId,
        description = description,
        params = params?.stringParams,
        departureId = departureId,
        arrivalId = arrivalId,
        departureUtcTime = DateTimeUtils.strLogTime(departureUtcTime ?: 0),
        arrivalUtcTime = DateTimeUtils.strLogTime(arrivalUtcTime ?: 0),
    )