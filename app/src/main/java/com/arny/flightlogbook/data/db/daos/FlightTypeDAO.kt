package com.arny.flightlogbook.data.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.arny.flightlogbook.data.models.FlightType

@Dao
interface FlightTypeDAO : BaseDao<FlightType> {
    @get:Query("SELECT * FROM flight_type")
    val flightTypes: List<FlightType>

    @Query("SELECT * FROM flight_type WHERE _id=:id")
    fun getFlightType(id: Long): FlightType

    @Query("DELETE FROM flight_type WHERE _id=:id")
    fun delete(id: Long): Int

    @Query("DELETE FROM flight_type")
    fun delete(): Int
}
