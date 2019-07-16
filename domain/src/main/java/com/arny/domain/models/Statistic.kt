package com.arny.domain.models

import com.arny.helpers.utils.Utility

class Statistic {
    var totalByMonth: Int = 0
    var cnt: Int = 0
    var daysTime: Int = 0
    var nightsTime: Int = 0
    var ifrTime: Int = 0
    var vfrTime: Int = 0
    var circleTime: Int = 0
    var zoneTime: Int = 0
    var marshTime: Int = 0
    var dt: Long = 0
    var strMoths: String? = null
    var strTotalByMonths: String? = null
    var dnTime: String? = null
    var ivTime: String? = null
    var czmTime: String? = null

    override fun toString(): String {
        return Utility.getFields(this)
    }
}
