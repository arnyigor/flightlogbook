package com.arny.flightlogbook.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arny.flightlogbook.data.db.intity.flights.FlightEntity
import com.arny.flightlogbook.data.models.flights.FlightTypeEntity

@Dao
interface FlightTypeDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplace(items: Collection<FlightTypeEntity>): Array<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: FlightTypeEntity): Long

    @Query("SELECT * FROM flight_type")
    fun queryTypes(): List<FlightTypeEntity>

    @Query("SELECT * FROM flight_type WHERE _id=:id")
    fun queryFlightType(id: Long?): FlightTypeEntity?

    @Query("UPDATE flight_type SET title=:title WHERE _id=:id ")
    fun setTitle(id: Long?, title: String?): Int

    @Query("DELETE FROM flight_type WHERE _id=:id")
    fun delete(id: Long?): Int

    @Query("DELETE FROM flight_type")
    fun delete(): Int
}
