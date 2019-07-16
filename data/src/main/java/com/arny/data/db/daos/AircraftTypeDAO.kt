package com.arny.data.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.database.Cursor
import com.arny.data.models.PlaneTypeEntity


@Dao
interface AircraftTypeDAO:BaseDao<PlaneTypeEntity> {
    @Query("SELECT * FROM type_table")
    fun queryAircraftTypes(): List<PlaneTypeEntity>

    @Query("SELECT * FROM type_table WHERE type_id=:id")
    fun queryAircraftType(id: Long?): PlaneTypeEntity?

    @Query("SELECT COUNT(*) FROM type_table")
    fun queryAirplaneTypesCount(): Cursor

    @Query("DELETE FROM type_table WHERE type_id=:id")
    fun delete(id: Long?): Int

     @Query("UPDATE type_table SET airplane_type=:title WHERE type_id=:id ")
	fun setTitle(id: Long?, title: String?): Long

    @Query("DELETE FROM type_table")
    fun delete(): Int
}
