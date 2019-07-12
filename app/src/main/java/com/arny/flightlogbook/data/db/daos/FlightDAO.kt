package com.arny.flightlogbook.data.db.daos

import android.arch.persistence.room.*
import android.database.Cursor
import com.arny.flightlogbook.data.models.Flight
import java.util.*

@Dao
interface FlightDAO : BaseDao<Flight> {
    @Query("SELECT * FROM main_table")
    fun queryFlights(): List<Flight>

    @Query("SELECT * FROM main_table ORDER BY :orderby")
    fun queryFlightsWithOrder(orderby: String): List<Flight>

    @Query("SELECT * FROM main_table WHERE _id=:id")
    fun queryFlight(id: Long): Flight

    @Query("SELECT SUM(log_time) FROM main_table")
    fun queryFlightTime(): Cursor

    @Query("SELECT COUNT(*) FROM main_table")
    fun queryFlightsCount(): Cursor

    @Query("DELETE FROM main_table WHERE _id=:id")
    fun delete(id: Long): Int

    @Query("DELETE FROM main_table")
    fun delete(): Int
}
