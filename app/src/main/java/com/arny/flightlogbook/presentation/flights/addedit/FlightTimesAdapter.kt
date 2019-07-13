package com.arny.flightlogbook.presentation.flights.addedit

import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.db.intities.TimeToFlightEntity
import com.arny.flightlogbook.data.utils.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.data.utils.setVisible
import kotlinx.android.synthetic.main.flight_times_item_layout.view.*


/**
 *Created by Sedoy on 12.07.2019
 */
class FlightTimesAdapter : SimpleAbstractAdapter<TimeToFlightEntity>() {

    override fun getLayout(viewType: Int): Int {
        return R.layout.flight_times_item_layout
    }

    fun setViewHolderListener(listener: FlightTimesClickListener) {
        this.listener = listener
    }

    interface FlightTimesClickListener : OnViewHolderListener<TimeToFlightEntity> {
        fun onTimeIncludeToflight(position: Int, item: TimeToFlightEntity)
        fun onTimeExcludeflight(position: Int, item: TimeToFlightEntity)
    }

    override fun getDiffCallback(): DiffCallback<TimeToFlightEntity>? {
        return object : DiffCallback<TimeToFlightEntity>() {
            override fun areItemsTheSame(oldItem: TimeToFlightEntity, newItem: TimeToFlightEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: TimeToFlightEntity, newItem: TimeToFlightEntity): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun bindView(item: TimeToFlightEntity, viewHolder: VH) {
        viewHolder.itemView.apply {
            tv_title.text = item.timeTypeEntity?.title
            tv_time.text = item.time.toString()
            tv_remove_from_flight.setVisible(!item.addToFlightTime)
            tv_add_to_flight.setVisible(item.addToFlightTime)
            tv_add_to_flight.setOnClickListener { }
            tv_remove_from_flight.setOnClickListener { }
            setOnClickListener { }
        }
    }

}