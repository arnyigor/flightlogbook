package com.arny.flightlogbook.data.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.arny.flightlogbook.utils.Utility

@Entity(tableName = "type_table")
data class AircraftType(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "type_id") var typeId: Long = 0) {
    @ColumnInfo(name = "airplane_type")
    var typeName: String? = null

    override fun toString(): String {
        return Utility.getFields(this)
    }
}
