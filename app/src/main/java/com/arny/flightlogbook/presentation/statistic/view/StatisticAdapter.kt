package com.arny.flightlogbook.presentation.statistic.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arny.core.utils.fromHtml
import com.arny.flightlogbook.adapters.diffUtilCallback
import com.arny.flightlogbook.databinding.StatisticItemLayoutBinding
import com.arny.flightlogbook.domain.models.Statistic
import com.arny.flightlogbook.domain.models.StatisticType

class StatisticAdapter : ListAdapter<Statistic, StatisticAdapter.StatisticViewHolder>(
    diffUtilCallback<Statistic>()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StatisticViewHolder = StatisticViewHolder(
        StatisticItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: StatisticViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StatisticViewHolder(private val binding: StatisticItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Statistic) {
            if (item.type == StatisticType.STARTTIME) {
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