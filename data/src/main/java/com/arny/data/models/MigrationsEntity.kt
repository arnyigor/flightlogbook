package com.arny.data.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.arny.helpers.utils.Utility

@Entity(tableName = "migrations")
data class MigrationsEntity(@PrimaryKey(autoGenerate = true)
                      @ColumnInfo(name = "_id")
                      var id: Long = 0) {
    var filename: String = ""
    var applytime: String = ""

    override fun toString(): String {
        return Utility.getFields(this)
    }
}
