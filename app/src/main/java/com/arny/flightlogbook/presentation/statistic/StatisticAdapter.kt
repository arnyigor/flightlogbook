package com.arny.flightlogbook.presentation.statistic

import android.annotation.SuppressLint
import com.arny.adapters.SimpleAbstractAdapter
import com.arny.domain.models.Statistic
import com.arny.flightlogbook.R
import com.arny.helpers.utils.fromHtml
import kotlinx.android.synthetic.main.statistic_item_layout.view.*

class StatisticAdapter :SimpleAbstractAdapter<Statistic>(){
    override fun getLayout(viewType: Int): Int {
        return R.layout.statistic_item_layout
    }

    override fun getDiffCallback(): DiffCallback<Statistic>? {
        return object : DiffCallback<Statistic>() {
            override fun areItemsTheSame(oldItem: Statistic, newItem: Statistic): Boolean {
                 return oldItem==newItem
            }

            override fun areContentsTheSame(oldItem: Statistic, newItem: Statistic): Boolean {
                return oldItem==newItem
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindView(item: Statistic, viewHolder: VH) {
        viewHolder.itemView.apply {
            if (item.type==0) {
                tv_stat_date.text = item.dateTimeStart
            }else{
                tv_stat_date.text = "${item.dateTimeStart}\n${item.dateTimeEnd}"
            }
            tv_stat_data.text = item.data?.let { fromHtml(it) }
        }
    }
}