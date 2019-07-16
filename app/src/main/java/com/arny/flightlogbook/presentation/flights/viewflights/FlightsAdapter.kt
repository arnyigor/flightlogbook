package com.arny.flightlogbook.presentation.flights.viewflights

import com.arny.adapters.SimpleAbstractAdapter
import com.arny.domain.models.Flight
import com.arny.flightlogbook.R
import kotlinx.android.synthetic.main.flight_list_item.view.*

class FlightsAdapter: SimpleAbstractAdapter<Flight>() {
    override fun getLayout(viewType: Int): Int {
        return R.layout.flight_list_item
    }

    override fun bindView(item: Flight, viewHolder: VH) {
        viewHolder.itemView.apply {
            val datetime = item.datetime ?: 0
            if (datetime > 0) {
                tvDate.text = item.datetimeFormatted
            }
            tvLogTime.text = item.logtimeFormatted
            tvType.text = item.airplanetypetitle
            setOnClickListener {
                listener?.onItemClick(viewHolder.adapterPosition, item)
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<Flight>? {
        return object : DiffCallback<Flight>() {
            override fun areItemsTheSame(oldItem: Flight, newItem: Flight): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Flight, newItem: Flight): Boolean {
                return oldItem == newItem
            }
        }
    }

}
