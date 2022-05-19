package com.arny.flightlogbook.data.models.flights

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arny.core.utils.DateTimeUtils
import com.arny.core.utils.fromJson
import com.arny.flightlogbook.domain.models.Flight
import com.google.gson.Gson
import com.google.gson.GsonBuilder

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
        flight.flightTimeFormatted = logtime?.let { DateTimeUtils.strLogTime(it) }
        flight.regNo = regNo
        flight.totalTime = (logtime ?: 0) + (this.groundTime ?: 0)
        flight.nightTime = nightTime ?: 0
        flight.groundTime = groundTime ?: 0
        flight.planeId = planeId
        flight.daynight = daynight
        flight.ifrvfr = ifrvfr
        flight.flightTypeId = flighttype
        flight.description = description
        flight.customParams = params?.toParams()
        flight.departureId = departureId
        flight.arrivalId = arrivalId
        flight.departureUtcTime = DateTimeUtils.convertStringToTime(departureUtcTime)
        flight.arrivalUtcTime = DateTimeUtils.convertStringToTime(arrivalUtcTime)
        return flight
    }
}

fun Map<String, Any?>?.fromParams(): String? = this?.let {
    val mapAsString = StringBuilder("{")
    val size = this.keys.size
    for ((ind, key) in this.keys.withIndex()) {
        mapAsString.append("$key=${this[key]}")
        if (size > 1 && ind < size - 1) {
            mapAsString.append(",")
        }
    }
    return mapAsString.append("}").toString()
}

fun String?.toParams(): HashMap<String, Any?>? {
    val array = this?.trimStart('{')
        ?.trimEnd('}')
        ?.split(",")
        ?.toTypedArray()
    return array?.let {
        hashMapOf<String, Any?>().apply {
            for (s in array.iterator()) {
                val (key, value) = s.split("=")
                set(key, value)
            }
        }
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
        params = customParams?.fromParams(),
        departureId = departureId,
        arrivalId = arrivalId,
        departureUtcTime = DateTimeUtils.strLogTime(departureUtcTime ?: 0),
        arrivalUtcTime = DateTimeUtils.strLogTime(arrivalUtcTime ?: 0),
    )