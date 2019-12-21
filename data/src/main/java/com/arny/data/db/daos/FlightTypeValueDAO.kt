package com.arny.data.db.daos

import androidx.room.Dao
import androidx.room.Query
import com.arny.data.models.FlightTypeValueEntity

@Dao
interface FlightTypeValueDAO : BaseDao<FlightTypeValueEntity> {
    @Query("SELECT * FROM flight_type_values")
    fun queryFlightTypeValues(): List<FlightTypeValueEntity>

    @Query("SELECT * FROM flight_type_values WHERE _id=:id")
    fun queryFlightTypeValue(id: Long): FlightTypeValueEntity

    @Query("SELECT * FROM flight_type_values WHERE type=:id")
    fun queryFlightTypeValuesByType(id: Long): List<FlightTypeValueEntity>

    @Query("DELETE FROM flight_type_values WHERE _id=:id")
    fun delete(id: Long): Int

    @Query("DELETE FROM flight_type_values")
    fun delete(): Int
}
