package com.arny.data.db.intities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *Created by Sedoy on 12.07.2019
 */
@Entity(tableName = "time_types")
data class TimeTypeEntity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long? = null,
        var title: String? = null)