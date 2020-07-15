package com.arny.flightlogbook.data.db.daos

import androidx.room.Dao
import androidx.room.Query
import com.arny.flightlogbook.data.models.CustomFieldValueEntity

@Dao
interface CustomFieldValuesDAO : BaseDao<CustomFieldValueEntity> {
    @Query("SELECT * FROM custom_field_values WHERE externalId=:externalId ORDER BY _id")
    fun getDbCustomFieldValues(externalId: Long): List<CustomFieldValueEntity>

    @Query("UPDATE custom_field_values SET value=:value WHERE _id=:id ")
    fun setValue(id: Long?, value: String?): Int

    @Query("DELETE FROM custom_field_values WHERE _id=:id")
    fun delete(id: Long?): Int

    @Query("DELETE FROM custom_field_values")
    fun delete(): Int
}