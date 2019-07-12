package com.arny.flightlogbook.data.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.arny.flightlogbook.data.models.FlightTypeValue

@Dao
interface FlightTypeValueDAO : BaseDao<FlightTypeValue> {
    @Query("SELECT * FROM flight_type_values")
    fun queryFlightTypeValues(): List<FlightTypeValue>

    @Query("SELECT * FROM flight_type_values WHERE _id=:id")
    fun queryFlightTypeValue(id: Long): FlightTypeValue

    @Query("SELECT * FROM flight_type_values WHERE type=:id")
    fun queryFlightTypeValuesByType(id: Long): List<FlightTypeValue>

    @Query("DELETE FROM flight_type_values WHERE _id=:id")
    fun delete(id: Long): Int

    @Query("DELETE FROM flight_type_values")
    fun delete(): Int
}
