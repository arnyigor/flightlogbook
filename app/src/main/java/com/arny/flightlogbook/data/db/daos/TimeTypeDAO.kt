package com.arny.flightlogbook.data.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import android.database.Cursor
import com.arny.flightlogbook.data.db.intities.TimeTypeEntity

import com.arny.flightlogbook.data.models.PlaneType

@Dao
interface TimeTypeDAO:BaseDao<TimeTypeEntity> {
    @Query("SELECT * FROM time_types")
    fun queryTimeTypes(): List<TimeTypeEntity>

    @Query("SELECT * FROM time_types WHERE flight=:flightId")
    fun queryTimeTypes(flightId:Long?): List<TimeTypeEntity>

    @Query("SELECT * FROM time_types WHERE _id=:id")
    fun queryTimeType(id: Long): TimeTypeEntity?

    @Query("UPDATE time_types SET title=:title WHERE _id=:id ")
	fun setTitle(id: Long?, title: String?): Long

    @Query("UPDATE time_types SET flight=:flight WHERE _id=:id ")
	fun setFlight(id: Long?, flight: Long?): Long

    @Query("DELETE FROM time_types")
    fun delete(): Int

    @Query("DELETE FROM time_types WHERE _id=:id")
    fun delete(id: Long?): Int
}
