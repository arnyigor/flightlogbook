package com.arny.flightlogbook.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_fields")
data class CustomFieldEntity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        val id: Long? = null,
        val name: String? = null,
        val type: String? = null,
        @ColumnInfo(name = "show_by_default")
        val showByDefault: Boolean? = null
)
