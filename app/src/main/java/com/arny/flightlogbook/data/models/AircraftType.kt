package com.arny.flightlogbook.data.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.arny.arnylib.utils.Utility
import org.chalup.microorm.annotations.Column

@Entity(tableName = "type_table")
data class AircraftType(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "type_id") var typeId: Int = 0) {
    constructor() : this(0)

    @ColumnInfo(name = "airplane_type")
    var typeName: String? = null

    override fun toString(): String {
        return Utility.getFields(this)
    }
}
