package com.arny.flightlogbook.data.models.planes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
        tableName = "type_table",
        indices = [Index("airplane_type", "reg_no", unique = true)]
)
data class PlaneTypeEntity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "type_id") var typeId: Long? = null,
        @ColumnInfo(name = "airplane_type")
        var typeName: String? = null,
        @ColumnInfo(name = "reg_no")
        var regNo: String? = null,
        @ColumnInfo(name = "main_type")
        var mainType: String? = null
)
