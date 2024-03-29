package com.arny.flightlogbook.data.db.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.arny.flightlogbook.data.models.customfields.CustomFieldEntity
import com.arny.flightlogbook.data.models.customfields.FieldWithValues

@Dao
interface CustomFieldDAO : BaseDao<CustomFieldEntity> {
    @Query("SELECT * FROM custom_fields")
    fun getDbCustomFields(): List<CustomFieldEntity>

    @Query("SELECT * FROM custom_fields WHERE add_time=='1'")
    fun getDbCustomFieldsAddTime(): List<CustomFieldEntity>

    @Query("SELECT * FROM custom_fields WHERE _id =:id")
    fun getDbCustomField(id: Long): CustomFieldEntity?

    @Query("DELETE FROM custom_fields WHERE _id=:id")
    fun delete(id: Long?): Int

    @Query("SELECT * from custom_fields")
    @Transaction
    fun getFieldsWithValues(): List<FieldWithValues>

    @Query("DELETE FROM custom_fields")
    fun delete(): Int
}
