package com.arny.flightlogbook.presentation.flights.addedit

import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.db.intities.TimeTypeEntity
import com.arny.flightlogbook.data.utils.adapters.SimpleAbstractAdapter


/**
 *Created by Sedoy on 12.07.2019
 */
class FlightTimesAdapter : SimpleAbstractAdapter<TimeTypeEntity>() {

    override fun getLayout(viewType: Int): Int {
        return R.layout.flight_times_item_layout
    }

    override fun bindView(item: TimeTypeEntity, viewHolder: VH) {
        viewHolder.itemView.apply {
            //UI setting code
        }
    }

}