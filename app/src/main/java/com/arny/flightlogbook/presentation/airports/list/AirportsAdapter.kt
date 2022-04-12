package com.arny.flightlogbook.presentation.airports.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arny.core.utils.diffUtilCallback
import com.arny.core.utils.ifNull
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.IAirportBinding
import com.arny.flightlogbook.domain.models.Airport

class AirportsAdapter(
    private val isRus: Boolean,
    private val onClick: (position: Int, item: Airport) -> Unit
) :
    ListAdapter<Airport, AirportsAdapter.AdapterViewholder>(
        diffUtilCallback<Airport> { firstItem, secondItem -> firstItem == secondItem }
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterViewholder = AdapterViewholder(
        IAirportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: AdapterViewholder, position: Int) {
        holder.bind(getItem(holder.adapterPosition))
    }

    inner class AdapterViewholder(private val binding: IAirportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Airport) {
            val root = binding.root
            val context = root.context
            val position = adapterPosition
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
            root.setOnClickListener { onClick(position, item) }
        }
    }

}
