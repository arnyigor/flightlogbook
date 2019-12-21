package com.arny.data.db.daos

import androidx.room.Dao
import androidx.room.Query
import android.database.Cursor
import com.arny.data.db.intities.TimeToFlightEntity

@Dao
interface TimeToFlightsDAO : BaseDao<TimeToFlightEntity> {
    @Query("SELECT * FROM times_to_flights")
    fun queryTimesToFlights(): List<TimeToFlightEntity>

    @Query("SELECT * FROM times_to_flights WHERE flight=:flightId")
    fun queryTimesOfFlight(flightId: Long?): List<TimeToFlightEntity>

    @Query("SELECT * FROM times_to_flights WHERE _id=:id")
    fun queryTimeToFlight(id: Long): TimeToFlightEntity?

    @Query("SELECT SUM(time) FROM times_to_flights WHERE flight=:flight AND add_flight_time =:addToFlight")
    fun queryFlightTimesSum(flight: Long?, addToFlight: Boolean): Cursor

    @Query("SELECT SUM(time) FROM times_to_flights WHERE add_flight_time =:addToFlight")
    fun queryFlightTimesSum(addToFlight: Boolean): Cursor

    @Query("SELECT SUM(time) FROM times_to_flights")
    fun queryFlightTimesSum(): Cursor

    @Query("UPDATE times_to_flights SET time=:time,add_flight_time=:addToFlightTime WHERE flight=:flightId AND time_type=:timeTypeId ")
    fun setFlightTime(flightId: Long?, timeTypeId: Long?, time: Int, addToFlightTime: Boolean): Long

    @Query("UPDATE times_to_flights SET time_type=:timeType, flight=:flight,time=:time,add_flight_time =:addToFlightTime  WHERE _id=:id")
    fun setTimeToFlight(id: Long?, timeType: Long?, flight: Long?, time: Long, addToFlightTime: Boolean): Long

    @Query("DELETE FROM times_to_flights WHERE _id=:id")
    fun delete(id: Long?): Int

    @Query("DELETE FROM times_to_flights WHERE flight=:flight")
    fun deleteTimesFromFlight(flight: Long?): Int

    @Query("DELETE FROM times_to_flights WHERE flight=:flight AND time_type=:timeType")
    fun deleteTimeFromFlight(flight: Long?, timeType: Long?): Int
}
