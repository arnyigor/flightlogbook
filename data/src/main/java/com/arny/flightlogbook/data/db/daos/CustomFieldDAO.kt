package com.arny.flightlogbook.data.db.daos

import androidx.room.Dao
import androidx.room.Query
import com.arny.flightlogbook.data.models.CustomFieldEntity

@Dao
interface CustomFieldDAO : BaseDao<CustomFieldEntity> {
    @Query("SELECT * FROM custom_fields")
    fun getDbCustomFields(): List<CustomFieldEntity>

    @Query("SELECT * FROM custom_fields WHERE _id =:id")
    fun getDbCustomField(id: Long): CustomFieldEntity?

    @Query("DELETE FROM custom_fields WHERE _id=:id")
    fun delete(id: Long?): Int

    @Query("DELETE FROM custom_fields")
    fun delete(): Int
}
