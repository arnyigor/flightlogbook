package com.arny.flightlogbook.data.db.intity.customfields

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.arny.flightlogbook.data.db.intity.flights.FlightEntity

@Entity(
        tableName = "custom_field_values",
        foreignKeys = [
            ForeignKey(
                    entity = CustomFieldEntity::class,
                    parentColumns = ["_id"],
                    childColumns = ["fieldId"],
                    onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                    entity = FlightEntity::class,
                    parentColumns = ["_id"],
                    childColumns = ["externalId"],
                    onDelete = ForeignKey.CASCADE
            )
        ]
)
data class CustomFieldValueEntity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        val id: Long? = null,
        val fieldId: Long? = null,
        val externalId: Long? = null,
        val type: String?,
        val value: String? = null
)
