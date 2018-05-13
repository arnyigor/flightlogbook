package com.arny.flightlogbook.data.models

import com.arny.arnylib.utils.Utility
import org.chalup.microorm.annotations.Column

class Type {
    @Column("type_id")
    var typeId: Int = 0
    @Column("airplane_type")
    var typeName: String? = null

    override fun toString(): String {
        return Utility.getFields(this)
    }
}
