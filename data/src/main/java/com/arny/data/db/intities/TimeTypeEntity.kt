package com.arny.data.db.intities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

/**
 *Created by Sedoy on 12.07.2019
 */
@Entity(tableName = "time_types")
data class TimeTypeEntity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long? = null,
        var title: String? = null,
        @Ignore
        var selected: Boolean = false)