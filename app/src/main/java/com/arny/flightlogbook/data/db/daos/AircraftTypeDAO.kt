package com.arny.flightlogbook.data.db.daos

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.arny.flightlogbook.data.models.planes.PlaneTypeEntity

@Dao
interface AircraftTypeDAO  {

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateReplace(item: PlaneTypeEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplace(item: PlaneTypeEntity): Long

    @Query("SELECT * FROM type_table")
    fun queryAircraftTypes(): List<PlaneTypeEntity>

    @Query("SELECT * FROM type_table WHERE type_id=:id")
    fun queryAircraftType(id: Long?): PlaneTypeEntity?

    @Query("SELECT * FROM type_table WHERE airplane_type=:title")
    fun queryAircraftType(title: String?): PlaneTypeEntity?

    @Query("SELECT * FROM type_table WHERE reg_no=:regNo")
    fun queryAircraftByRegNo(regNo: String?): PlaneTypeEntity?

    @Query("SELECT COUNT(*) FROM type_table")
    fun queryAirplaneTypesCount(): Cursor

    @Query("DELETE FROM type_table WHERE type_id=:id")
    fun delete(id: Long?): Int

    @Query("UPDATE type_table SET airplane_type=:title WHERE type_id=:id ")
    fun setTitle(id: Long?, title: String?): Int

    @Query("DELETE FROM type_table")
    fun delete(): Int
}
