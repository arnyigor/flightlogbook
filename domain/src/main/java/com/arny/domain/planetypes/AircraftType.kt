package com.arny.domain.planetypes

import androidx.annotation.StringRes
import com.arny.domain.R

enum class AircraftType(@StringRes val nameRes: Int) {
    AIRPLANE(R.string.plane_main_type_airplane),
    HELICOPTER(R.string.plane_main_type_helicoper),
    GLIDER(R.string.plane_main_type_glider),
    AUTOGYRO(R.string.plane_main_type_autogyro),
    AEROSTAT(R.string.plane_main_type_aerostat),
    AIRSHIP(R.string.plane_main_type_airship),
}
