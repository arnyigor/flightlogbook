package com.arny.flightlogbook.presentation.airports

import com.arny.domain.models.Airport
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import kotlinx.android.synthetic.main.i_airport.view.*

class AirportsAdapter : SimpleAbstractAdapter<Airport>() {
    override fun getLayout(viewType: Int): Int = R.layout.i_airport

    override fun getDiffCallback() = object : DiffCallback<Airport>() {
        override fun areItemsTheSame(oldItem: Airport, newItem: Airport) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Airport, newItem: Airport) = oldItem == newItem
    }

    override fun bindView(item: Airport, viewHolder: VH) {
        viewHolder.itemView.apply {
            tvIcao.text = item.icao
            tvIata.text = item.iata
        }
    }
}