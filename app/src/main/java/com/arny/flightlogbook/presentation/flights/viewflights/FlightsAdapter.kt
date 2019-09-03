package com.arny.flightlogbook.presentation.flights.viewflights

import com.arny.adapters.SimpleAbstractAdapter
import com.arny.domain.models.Flight
import com.arny.flightlogbook.R
import com.arny.helpers.utils.DateTimeUtils
import kotlinx.android.synthetic.main.flight_list_item.view.*

class FlightsAdapter : SimpleAbstractAdapter<Flight>() {
    override fun getLayout(viewType: Int): Int {
        return R.layout.flight_list_item
    }

    override fun bindView(item: Flight, viewHolder: VH) {
        val position = viewHolder.adapterPosition
        viewHolder.itemView.apply {
            val datetime = item.datetime ?: 0
            if (datetime > 0) {
                tv_date.text = item.datetimeFormatted
            }
            val strLogTime = DateTimeUtils.strLogTime(item.sumlogTime ?: 0)
            val strFlightTime = DateTimeUtils.strLogTime(item.sumFlightTime ?: 0)
            tv_log_time_flight.text = strFlightTime
            tv_log_time_flight_total.text = strLogTime
            tv_flight_type.text = item.flightType?.typeTitle
            tv_plane_reg_no.text = item.regNo
            tv_plane_type.text = item.planeTitle
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
