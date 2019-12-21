package com.arny.data.db.daos

import androidx.room.Dao
import androidx.room.Query
import com.arny.data.models.FlightTypeEntity

@Dao
interface FlightTypeDAO : BaseDao<FlightTypeEntity> {
    @Query("SELECT * FROM flight_type")
    fun queryTypes(): List<FlightTypeEntity>

    @Query("SELECT * FROM flight_type WHERE _id=:id")
    fun queryFlightType(id: Long?): FlightTypeEntity?

    @Query("UPDATE flight_type SET title=:title WHERE _id=:id ")
    fun setTitle(id: Long?, title: String?): Long

    @Query("DELETE FROM flight_type WHERE _id=:id")
    fun delete(id: Long?): Int

    @Query("DELETE FROM flight_type")
    fun delete(): Int
}
