package com.arny.data.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.arny.data.db.intities.TimeToFlightEntity

@Dao
interface TimeToFlightsDAO : BaseDao<TimeToFlightEntity> {
    @Query("SELECT * FROM times_to_flights")
    fun queryTimesToFlights(): List<TimeToFlightEntity>

    @Query("SELECT * FROM times_to_flights WHERE flight=:flightId")
    fun queryTimesOfFlight(flightId: Long?): List<TimeToFlightEntity>

    @Query("SELECT * FROM times_to_flights WHERE _id=:id")
    fun queryTimeToFlight(id: Long): TimeToFlightEntity?

    @Query("UPDATE times_to_flights SET time=:time,add_flight_time=:addToFlightTime WHERE flight=:flightId AND time_type=:timeTypeId ")
    fun setFlightTime(flightId: Long?, timeTypeId: Long?, time: Long, addToFlightTime: Boolean): Long

    @Query("UPDATE times_to_flights SET time_type=:timeType, flight=:flight,time=:time,add_flight_time =:addToFlightTime  WHERE _id=:id")
    fun setTimeToFlight(id: Long?, timeType: Long?, flight: Long?, time: Long, addToFlightTime: Boolean): Long

    @Query("DELETE FROM times_to_flights")
    fun delete(): Int

    @Query("DELETE FROM times_to_flights WHERE _id=:id")
    fun delete(id: Long?): Int

    @Query("DELETE FROM times_to_flights WHERE flight=:flight")
    fun deleteTimesFromFlight(flight: Long?): Int

    @Query("DELETE FROM times_to_flights WHERE flight=:flight AND time_type=:timeType")
    fun deleteTimeFromFlight(flight: Long?, timeType: Long?): Int
}