package com.arny.flightlogbook.data.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.arny.arnylib.utils.Utility

@Entity(tableName = "migrations")
data class Migrations(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var id: Long = 0) {
    constructor() : this(0)
    var filename: String? = null
    var applytime: String? = null

    override fun toString(): String {
        return Utility.getFields(this)
    }
}
