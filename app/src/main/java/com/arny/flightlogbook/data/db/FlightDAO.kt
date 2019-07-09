package com.arny.flightlogbook.data.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import android.database.Cursor

import com.arny.flightlogbook.data.models.Flight

import java.util.ArrayList

@Dao
interface FlightDAO {
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(flight: Flight): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(flight: Flight): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(flights: ArrayList<Flight>): LongArray
}
