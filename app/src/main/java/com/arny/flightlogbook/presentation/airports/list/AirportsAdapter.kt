package com.arny.flightlogbook.presentation.airports.list

import com.arny.domain.models.Airport
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.helpers.utils.ifNull
import kotlinx.android.synthetic.main.i_airport.view.*

class AirportsAdapter(private val isRus: Boolean) : SimpleAbstractAdapter<Airport>() {
    override fun getLayout(viewType: Int): Int = R.layout.i_airport

    override fun getDiffCallback() = object : DiffCallback<Airport>() {
        override fun areItemsTheSame(oldItem: Airport, newItem: Airport) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Airport, newItem: Airport) = oldItem == newItem
    }

    override fun bindView(item: Airport, viewHolder: VH) {
        val position = viewHolder.adapterPosition
        viewHolder.itemView.apply {
            tvAirportCodes.text = context.getString(
                    R.string.string_format_two_strings,
                    item.iata,
                    "(${item.icao})"
            )
            tvAirportName.text = context.getString(
                    R.string.string_format_two_strings,
                    item.nameEng,
                    if (item.nameRus.isNullOrBlank()) "" else "(${item.nameRus})"
            )
            val sityName = if (isRus) item.cityRus?.ifNull("") else item.cityEng?.ifNull("")
            val countryName = if (isRus) item.countryRus?.ifNull("") else item.countryEng?.ifNull("")
            tvCity.text = context.getString(R.string.string_format_two_strings, sityName, "(${countryName})")
            setOnClickListener { listener?.onItemClick(position, item) }
        }
    }
}
