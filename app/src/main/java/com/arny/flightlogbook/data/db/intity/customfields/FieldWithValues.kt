package com.arny.flightlogbook.data.db.intity.customfields

import androidx.room.Embedded
import androidx.room.Relation

class FieldWithValues {
    @Embedded
    var field: CustomFieldEntity? = null

    @Relation(
            parentColumn = "_id",
            entityColumn = "fieldId",
            entity = CustomFieldValueEntity::class
    )
    var values: List<CustomFieldValueEntity>? = null
}