package com.arny.flightlogbook.presentation.statistic.view

import com.arny.core.utils.fromHtml
import com.arny.domain.models.Statistic
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import kotlinx.android.synthetic.main.statistic_item_layout.view.*

class StatisticAdapter : SimpleAbstractAdapter<Statistic>() {
    override fun getLayout(viewType: Int) = R.layout.statistic_item_layout

    override fun getDiffCallback(): DiffCallback<Statistic>? {
        return object : DiffCallback<Statistic>() {
            override fun areItemsTheSame(oldItem: Statistic, newItem: Statistic) = oldItem == newItem
            override fun areContentsTheSame(oldItem: Statistic, newItem: Statistic) = oldItem == newItem
        }
    }

    override fun bindView(item: Statistic, viewHolder: VH) {
        viewHolder.itemView.apply {
            if (item.type == 0) {
                tv_stat_date.text = item.dateTimeStart
            } else {
                tv_stat_date.text = StringBuilder().apply {
                    append(item.dateTimeStart)
                    append("\n")
                    append(item.dateTimeEnd)
                }.toString()
            }
            tv_stat_data.text = item.data?.let { fromHtml(it) }
        }
    }
}