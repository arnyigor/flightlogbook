package com.arny.domain.models

data class StatisticFilter(var type: FilterType = FilterType.FLIGHT, var id: Long? = null, var title: String? = null)