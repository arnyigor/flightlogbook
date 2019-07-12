package com.arny.flightlogbook.data.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import com.arny.flightlogbook.data.utils.Utility

@Entity(tableName = "flight_type_values",
        foreignKeys = [ForeignKey(entity = FlightType::class, parentColumns = ["_id"], childColumns = ["type"], onDelete = ForeignKey.CASCADE)])
data class FlightTypeValue(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var id: Long? = null) {
    @ColumnInfo(name = "type")
    var type: Long? = null
    @ColumnInfo(name = "type_value")
    var typeValue: String? = null
    @ColumnInfo(name = "type_title")
    var typeTitle: String? = null

    override fun toString(): String {
        return Utility.getFields(this)
    }
}
