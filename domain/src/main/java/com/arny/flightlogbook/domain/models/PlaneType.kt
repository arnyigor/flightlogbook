package com.arny.flightlogbook.domain.models

import com.arny.flightlogbook.domain.planetypes.AircraftType
import java.io.Serializable

data class PlaneType(
        var typeId: Long? = null,
        var typeName: String? = null,
        var mainType: AircraftType? = null,
        var regNo: String? = null
):Serializable
