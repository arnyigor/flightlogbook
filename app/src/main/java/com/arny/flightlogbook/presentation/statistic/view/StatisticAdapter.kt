package com.arny.flightlogbook.presentation.statistic.view

import android.view.LayoutInflater
import android.view.ViewGroup
import com.arny.core.utils.fromHtml
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.databinding.StatisticItemLayoutBinding
import com.arny.flightlogbook.domain.models.Statistic

class StatisticAdapter : SimpleAbstractAdapter<Statistic>() {
    private lateinit var binding: StatisticItemLayoutBinding

    override fun getLayout(viewType: Int) = R.layout.statistic_item_layout

    override fun getDiffCallback(): DiffCallback<Statistic> {
        return object : DiffCallback<Statistic>() {
            override fun areItemsTheSame(oldItem: Statistic, newItem: Statistic) =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: Statistic, newItem: Statistic) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        binding = StatisticItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding.root)
    }

    override fun bindView(item: Statistic, viewHolder: VH) {
        viewHolder.itemView.apply {
            if (item.type == 0) {
                binding.tvStatDate.text = item.dateTimeStart
            } else {
                binding.tvStatDate.text = StringBuilder().apply {
                    append(item.dateTimeStart)
                    append("\n")
                    append(item.dateTimeEnd)
                }.toString()
            }
            binding.tvStatData.text = item.data?.let { fromHtml(it) }
        }
    }
}