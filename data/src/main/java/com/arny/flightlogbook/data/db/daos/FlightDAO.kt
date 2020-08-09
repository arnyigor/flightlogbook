package com.arny.flightlogbook.data.db.daos

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Query
import com.arny.flightlogbook.data.models.FlightEntity

@Dao
interface FlightDAO : BaseDao<FlightEntity> {
    @Query("SELECT * FROM main_table")
    fun queryFlights(): List<FlightEntity>

    @Query("SELECT * FROM main_table ORDER BY :orderby")
    fun queryFlightsWithOrder(orderby: String): List<FlightEntity>

    @Query("SELECT * FROM main_table WHERE reg_no=:regNo")
    fun queryFlightsByRegNo(regNo: String?): List<FlightEntity>

    @Query("SELECT * FROM main_table WHERE _id=:id")
    fun queryFlight(id: Long?): FlightEntity?

    @Query("SELECT * FROM main_table WHERE datetime>=:startDate AND datetime<=:endDate")
    fun queryFlightsInclude(startDate: Long, endDate: Long): List<FlightEntity>

    @Query("SELECT * FROM main_table WHERE datetime>=:startDate AND datetime <:endDate")
    fun queryFlights(startDate: Long, endDate: Long): List<FlightEntity>

    @Query("SELECT * FROM main_table WHERE datetime>=:startDate AND datetime<=:endDate AND airplane_type IN(:planeTypes)")
    fun queryFlightsByPlanesIncludeEnd(startDate: Long, endDate: Long, planeTypes: List<Long?>): List<FlightEntity>

    @Query("SELECT * FROM main_table WHERE datetime>=:startDate AND datetime< :endDate AND airplane_type IN(:planeTypes)")
    fun queryFlightsByPlanes(startDate: Long, endDate: Long, planeTypes: List<Long?>): List<FlightEntity>

    @Query("SELECT * FROM main_table WHERE datetime>=:startDate AND datetime< :endDate AND params MATCH :hexColor")
    fun queryFlightsByColor(startDate: Long, endDate: Long, hexColor: String): List<FlightEntity>

    @Query("SELECT * FROM main_table WHERE datetime>=:startDate AND datetime<=:endDate AND params IS NOT NULL AND params LIKE :hexColor")
    fun queryFlightsByColorInclude(startDate: Long, endDate: Long, hexColor: String): List<FlightEntity>

    @Query("SELECT * FROM main_table WHERE datetime>=:startDate AND datetime<=:endDate AND flight_type IN(:flightTypes)")
    fun queryFlightsByFlightTypesInclude(startDate: Long, endDate: Long, flightTypes: List<Long?>): List<FlightEntity>

    @Query("SELECT * FROM main_table WHERE datetime>=:startDate AND datetime < :endDate AND flight_type IN(:flightTypes)")
    fun queryFlightsByFlightTypes(startDate: Long, endDate: Long, flightTypes: List<Long?>): List<FlightEntity>

    @Query("SELECT min(datetime) as first, max(datetime) as last FROM main_table")
    fun queryMinMaxDateTimes(): Cursor

    @Query("SELECT params FROM main_table WHERE params IS NOT NULL AND params LIKE '%\"color\":\"#%'")
    fun queryNotEmptyColorParams(): Cursor

    @Query("SELECT SUM(log_time) FROM main_table")
    fun queryFlightTime(): Cursor

    @Query("SELECT SUM(night_time) FROM main_table")
    fun queryNightTime(): Cursor

    @Query("SELECT SUM(ground_time) FROM main_table")
    fun queryGroundTime(): Cursor

    @Query("SELECT COUNT(*) FROM main_table")
    fun queryFlightsCount(): Cursor

    @Query("DELETE FROM main_table WHERE _id=:id")
    fun delete(id: Long?): Int

    @Query("DELETE FROM main_table WHERE _id IN(:ids)")
    fun delete(ids: List<Long>): Int

    @Query("DELETE FROM main_table")
    fun delete(): Int
}
