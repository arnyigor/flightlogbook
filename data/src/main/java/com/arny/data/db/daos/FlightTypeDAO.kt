package com.arny.data.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.arny.data.models.FlightTypeEntity

@Dao
interface FlightTypeDAO : BaseDao<FlightTypeEntity> {
    @get:Query("SELECT * FROM flight_type")
    val flightTypes: List<FlightTypeEntity>

    @Query("SELECT * FROM flight_type WHERE _id=:id")
    fun getFlightType(id: Long): FlightTypeEntity

    @Query("DELETE FROM flight_type WHERE _id=:id")
    fun delete(id: Long): Int

    @Query("DELETE FROM flight_type")
    fun delete(): Int
}
