package com.arny.flightlogbook.presentation.flights.viewflights

import com.arny.adapters.SimpleAbstractAdapter
import com.arny.domain.models.Flight
import com.arny.flightlogbook.R
import kotlinx.android.synthetic.main.flight_list_item.view.*

class FlightsAdapter : SimpleAbstractAdapter<Flight>() {
    override fun getLayout(viewType: Int): Int = R.layout.flight_list_item
    override fun bindView(item: Flight, viewHolder: VH) {
        val position = viewHolder.adapterPosition
        viewHolder.itemView.apply {
            tvDate.text = item.datetimeFormatted
            tvFlightTime.text = item.logtimeFormatted ?: "00:00"
            tvTotalTime.text = item.totalTimeFormatted ?: "00:00"
            tvFlightTitle.text = item.flightType?.typeTitle
            tvPlaneRegNo.text = item.regNo
            tvPlaneType.text = item.planeType?.typeName
            item.colorText?.let {
                tvTotalTime.setTextColor(it)
                tvFlightTime.setTextColor(it)
                tvDate.setTextColor(it)
                tvPlaneRegNo.setTextColor(it)
                tvPlaneType.setTextColor(it)
            }
            item.colorInt?.takeIf { it != 0 && it != -1 }?.let {
                clFlightsItemContainer.setBackgroundColor(it)
            }
            setOnClickListener {
                listener?.onItemClick(position, item)
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
