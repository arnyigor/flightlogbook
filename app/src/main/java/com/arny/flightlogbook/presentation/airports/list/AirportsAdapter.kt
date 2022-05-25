package com.arny.flightlogbook.presentation.airports.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.diffUtilCallback
import com.arny.flightlogbook.databinding.IAirportBinding
import com.arny.flightlogbook.domain.models.Airport

class AirportsAdapter(
    private val isRus: Boolean,
    private val onClick: (position: Int, item: Airport) -> Unit
) : ListAdapter<Airport, AirportsAdapter.AdapterViewholder>(
    diffUtilCallback<Airport>(itemsTheSame = { old, new -> old.id == new.id })
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
        holder.bind(getItem(position))
    }

    inner class AdapterViewholder(private val binding: IAirportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Airport) {
            val root = binding.root
            val context = root.context
            val position = layoutPosition
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
            val cityName = if (isRus) item.cityRus.orEmpty() else item.cityEng.orEmpty()
            val countryName =
                if (isRus) item.countryRus.orEmpty() else item.countryEng.orEmpty()
            context.getString(R.string.string_format_two_strings, cityName, "(${countryName})")
            root.setOnClickListener { onClick(position, item) }
        }
    }
}
