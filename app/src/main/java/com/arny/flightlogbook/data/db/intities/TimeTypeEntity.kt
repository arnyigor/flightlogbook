package com.arny.flightlogbook.data.db.intities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 *Created by Sedoy on 12.07.2019
 */
@Entity(tableName = "time_types")
data class TimeTypeEntity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long? = null,
        var flight: Long? = null,
        var time: Long = 0,//minutes
        @ColumnInfo(name = "add_flight_time")
        var addToFlightTime: Boolean = false,
        var title: String? = null)