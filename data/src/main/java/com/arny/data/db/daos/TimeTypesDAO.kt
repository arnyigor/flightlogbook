package com.arny.data.db.daos

import androidx.room.Dao
import androidx.room.Query
import com.arny.data.db.intities.TimeTypeEntity

@Dao
interface TimeTypesDAO:BaseDao<TimeTypeEntity> {
    @Query("SELECT * FROM time_types")
    fun queryTimeTypes(): List<TimeTypeEntity>

    @Query("SELECT * FROM time_types WHERE _id=:id")
    fun queryTimeType(id: Long): TimeTypeEntity?

    @Query("UPDATE time_types SET title=:title WHERE _id=:id ")
	fun setTitle(id: Long?, title: String?): Long

    @Query("DELETE FROM time_types")
    fun delete(): Int

    @Query("DELETE FROM time_types WHERE _id=:id")
    fun delete(id: Long?): Int
}
