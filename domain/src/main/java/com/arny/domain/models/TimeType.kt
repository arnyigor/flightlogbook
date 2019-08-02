package com.arny.domain.models

import com.arny.data.db.intities.TimeTypeEntity

/**
 *Created by Sedoy on 12.07.2019
 */
data class TimeType(
        var id: Long? = null,
        var title: String? = null)

fun TimeType?.toTimeTypeEntity(): TimeTypeEntity {
    val typeEntity = TimeTypeEntity()
    typeEntity.id = this?.id
    typeEntity.title = this?.title
    return typeEntity
}

fun TimeTypeEntity?.toTimeType(): TimeType {
    val type = TimeType()
    type.id = this?.id
    type.title = this?.title
    return type
}