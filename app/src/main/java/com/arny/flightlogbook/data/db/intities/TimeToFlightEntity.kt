package com.arny.flightlogbook.data.db.intities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "times_to_flights")
data class TimeToFlightEntity(
        @PrimaryKey(autoGenerate = true)
        var _id: Long? = null,
        var flight: Long? = null,
        @ColumnInfo(name = "time_type")
        var timeType: Long? = null,
        @Ignore
        var timeTypeEntity: TimeTypeEntity? = null,
        var time: Long = 0,//minutes
        @ColumnInfo(name = "add_flight_time")
        var addToFlightTime: Boolean = false)