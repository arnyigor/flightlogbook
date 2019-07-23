package com.arny.flightlogbook.presentation.flights.addedit

import com.arny.adapters.SimpleAbstractAdapter
import com.arny.data.db.intities.TimeToFlightEntity
import com.arny.flightlogbook.R
import com.arny.helpers.utils.DateTimeUtils
import com.arny.helpers.utils.setVisible
import kotlinx.android.synthetic.main.flight_times_item_layout.view.*


/**
 *Created by Sedoy on 12.07.2019
 */
class FlightTimesAdapter(private val flightTimeListener: FlightTimesClickListener? = null) : SimpleAbstractAdapter<TimeToFlightEntity>() {

    override fun getLayout(viewType: Int): Int {
        return R.layout.flight_times_item_layout
    }

    interface FlightTimesClickListener : OnViewHolderListener<TimeToFlightEntity> {
        fun onEditFlightTime(position: Int, item: TimeToFlightEntity)
        fun onDeleteFlightTime(position: Int, item: TimeToFlightEntity)
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
        val position = viewHolder.adapterPosition
        viewHolder.itemView.apply {
            iv_time_inflight.setVisible(item.addToFlightTime)
            tv_title.text = item.timeTypeEntity?.title
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