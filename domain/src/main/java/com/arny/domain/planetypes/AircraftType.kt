package com.arny.domain.planetypes

import androidx.annotation.StringRes
import com.arny.domain.R

enum class AircraftType(@StringRes val nameRes: Int, val mainType: Int) {
    AIRPLANE(R.string.plane_main_type_airplane, 0),
    HELICOPTER(R.string.plane_main_type_helicoper, 1),
    GLIDER(R.string.plane_main_type_glider, 2),
    AUTOGYRO(R.string.plane_main_type_autogyro, 3),
    AEROSTAT(R.string.plane_main_type_aerostat, 4),
    AIRSHIP(R.string.plane_main_type_airship, 5);

    companion object {
        fun getType(index: Int): AircraftType {
            return values().find { it.mainType == index } ?: AIRPLANE
        }
    }
}
