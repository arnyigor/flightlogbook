package com.arny.flightlogbook.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.arny.flightlogbook.data.db.intity.customfields.CustomFieldEntity
import com.arny.flightlogbook.data.db.intity.customfields.FieldWithValues

@Dao
interface CustomFieldDAO  {

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateReplace(item: CustomFieldEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplace(item: CustomFieldEntity): Long

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
