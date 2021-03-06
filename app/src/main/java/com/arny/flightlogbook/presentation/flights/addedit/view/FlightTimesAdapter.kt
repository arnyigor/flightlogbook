package com.arny.flightlogbook.presentation.flights.addedit.view

import androidx.core.view.isVisible
import com.arny.core.utils.DateTimeUtils
import com.arny.domain.models.TimeToFlight
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import kotlinx.android.synthetic.main.flight_times_item_layout.view.*


/**
 *Created by Sedoy on 12.07.2019
 */
class FlightTimesAdapter(private val flightTimeListener: FlightTimesClickListener? = null) : SimpleAbstractAdapter<TimeToFlight>() {

    override fun getLayout(viewType: Int): Int {
        return R.layout.flight_times_item_layout
    }

    interface FlightTimesClickListener : OnViewHolderListener<TimeToFlight> {
        fun onEditFlightTime(position: Int, item: TimeToFlight)
        fun onDeleteFlightTime(position: Int, item: TimeToFlight)
    }

    override fun getDiffCallback(): DiffCallback<TimeToFlight>? {
        return object : DiffCallback<TimeToFlight>() {
            override fun areItemsTheSame(oldItem: TimeToFlight, newItem: TimeToFlight): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: TimeToFlight, newItem: TimeToFlight): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun bindView(item: TimeToFlight, viewHolder: VH) {
        val position = viewHolder.adapterPosition
        viewHolder.itemView.apply {
            iv_time_inflight.isVisible = item.addToFlightTime
            tv_time.text = DateTimeUtils.strLogTime(item.time)
            iv_time_delete.setOnClickListener {
                flightTimeListener?.onDeleteFlightTime(position, item)
            }
            iv_time_edit.setOnClickListener {
                flightTimeListener?.onEditFlightTime(position, item)
            }
        }
    }

}