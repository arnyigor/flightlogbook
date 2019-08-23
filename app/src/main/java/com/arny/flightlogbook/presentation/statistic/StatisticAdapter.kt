package com.arny.flightlogbook.presentation.statistic

import com.arny.adapters.SimpleAbstractAdapter
import com.arny.domain.models.Statistic
import com.arny.flightlogbook.R

class StatisticAdapter :SimpleAbstractAdapter<Statistic>(){
    override fun getLayout(viewType: Int): Int {
        return R.layout.stat_list_item
    }

    override fun bindView(item: Statistic, viewHolder: VH) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
