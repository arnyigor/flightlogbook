package com.arny.flightlogbook.presentation.statistic

import com.arny.adapters.SimpleAbstractAdapter
import com.arny.domain.models.Statistic
import com.arny.flightlogbook.R

class StatisticAdapter :SimpleAbstractAdapter<Statistic>(){
    override fun getLayout(viewType: Int): Int {
        return R.layout.statistic_item_layout
    }

    override fun bindView(item: Statistic, viewHolder: VH) {
        viewHolder.itemView.apply {

        }
    }
}