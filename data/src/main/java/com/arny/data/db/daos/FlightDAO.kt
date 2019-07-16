package com.arny.data.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.database.Cursor
import com.arny.data.models.FlightEntity

@Dao
interface FlightDAO : BaseDao<FlightEntity> {
    @Query("SELECT * FROM main_table")
    fun queryFlights(): List<FlightEntity>

    @Query("SELECT * FROM main_table ORDER BY :orderby")
    fun queryFlightsWithOrder(orderby: String): List<FlightEntity>

    @Query("SELECT * FROM main_table WHERE _id=:id")
    fun queryFlight(id: Long?): FlightEntity?

    @Query("SELECT SUM(log_time) FROM main_table")
    fun queryFlightTime(): Cursor

    @Query("SELECT COUNT(*) FROM main_table")
    fun queryFlightsCount(): Cursor

    @Query("DELETE FROM main_table WHERE _id=:id")
    fun delete(id: Long?): Int

    @Query("DELETE FROM main_table")
    fun delete(): Int
}
