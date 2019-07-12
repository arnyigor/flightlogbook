package com.arny.flightlogbook.data.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import android.database.Cursor

import com.arny.flightlogbook.data.models.PlaneType

@Dao
interface AircraftTypeDAO {
    @Query("SELECT * FROM type_table")
    fun queryAircraftTypes(): List<PlaneType>

    @Query("SELECT * FROM type_table WHERE type_id=:id")
    fun queryAircraftType(id: Long): PlaneType?

    @Query("SELECT COUNT(*) FROM type_table")
    fun queryAirplaneTypesCount(): Cursor

    @Query("DELETE FROM type_table WHERE type_id=:id")
    fun delete(id: Long?): Int

     @Query("UPDATE type_table SET airplane_type=:title WHERE type_id=:id ")
	fun setTitle(id: Long?, title: String?): Long

    @Query("DELETE FROM type_table")
    fun delete(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(aircraftType: PlaneType): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(aircraftType: PlaneType?): Int
}
