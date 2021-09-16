package com.arny.flightlogbook.presentation.airports.list

import android.view.LayoutInflater
import android.view.ViewGroup
import com.arny.core.utils.ifNull
import com.arny.domain.models.Airport
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.databinding.IAirportBinding


class AirportsAdapter(private val isRus: Boolean) : SimpleAbstractAdapter<Airport>() {
    private lateinit var binding: IAirportBinding

    override fun getLayout(viewType: Int): Int = R.layout.i_airport

    override fun getDiffCallback() = object : DiffCallback<Airport>() {
        override fun areItemsTheSame(oldItem: Airport, newItem: Airport) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Airport, newItem: Airport) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        binding = IAirportBinding.inflate(inflater, parent, false)
        return VH(binding.root)
    }

    override fun bindView(item: Airport, viewHolder: VH) {
        val position = viewHolder.adapterPosition
        viewHolder.itemView.apply {
            binding.tvAirportCodes.text = context.getString(
                R.string.string_format_two_strings,
                item.iata,
                "(${item.icao})"
            )
            binding.tvAirportName.text = context.getString(
                R.string.string_format_two_strings,
                item.nameEng,
                if (item.nameRus.isNullOrBlank()) "" else "(${item.nameRus})"
            )
            val sityName = if (isRus) item.cityRus?.ifNull("") else item.cityEng?.ifNull("")
            val countryName =
                if (isRus) item.countryRus?.ifNull("") else item.countryEng?.ifNull("")
            binding.tvCity.text =
                context.getString(R.string.string_format_two_strings, sityName, "(${countryName})")
            setOnClickListener { listener?.onItemClick(position, item) }
        }
    }
}
