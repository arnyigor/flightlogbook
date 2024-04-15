package com.arny.flightlogbook.domain.models

import com.arny.flightlogbook.data.models.FilterType

data class StatisticFilter(var type: FilterType = FilterType.FLIGHT_TYPE, var id: Long? = null, var title: String? = null)