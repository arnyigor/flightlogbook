package com.arny.flightlogbook.presentation.flights.viewflights.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arny.core.utils.DateTimeUtils
import com.arny.core.utils.getIntColor
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.diffUtilCallback
import com.arny.flightlogbook.databinding.FlightListItemBinding
import com.arny.flightlogbook.domain.models.Flight

class FlightsAdapter(
    val onItemClick: (position: Int, item: Flight) -> Unit,
    val onFlightSelect: (position: Int, item: Flight) -> Unit,
    val onFlightRemove: (item: Flight) -> Unit
) : ListAdapter<Flight, FlightsAdapter.AdapterViewHolder>(
    diffUtilCallback<Flight>(itemsTheSame = { old, new -> old.id == new.id })
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder =
        AdapterViewHolder(
            FlightListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AdapterViewHolder(private val binding: FlightListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Flight) {
            val view = binding.root
            val context = view.context
            val planeType = item.planeType
            with(binding) {
                val datetime = item.datetime ?: 0
                if (datetime > 0) {
                    tvDate.text = item.datetimeFormatted
                }
                tvLogTimeFlight.text = item.flightTimeFormatted
                tvLogTimeFlightTotal.text = DateTimeUtils.strLogTime(item.totalTime)
                tvFlightType.text = item.flightType?.typeTitle
                tvPlaneRegNo.text = item.regNo
                with(tvPlaneType) {
                    planeType?.let {
                        text = planeType.typeName
                    }
                    isVisible = planeType != null
                }
                tvDescr.isVisible = !item.description.isNullOrBlank()
                tvDescr.text = item.description
                var colorText = item.colorText ?: context.getIntColor(R.color.colorTextPrimary)
                if (item.selected) {
                    colorText = ContextCompat.getColor(context, R.color.colorTextPrimary)
                }
                clFlightsItemContainer.setBackgroundColor(getBgColor(item, context))
                colorText.let {
                    tvLogTimeFlightTotal.setTextColor(it)
                    tvLogTimeFlight.setTextColor(it)
                    tvDate.setTextColor(it)
                    tvPlaneRegNo.setTextColor(it)
                    tvPlaneType.setTextColor(it)
                    tvDescr.setTextColor(it)
                    tvFlightType.setTextColor(it)
                }
                view.setOnClickListener {
                    onItemClick(layoutPosition, item)
                }
                ivRemove.setOnClickListener {
                    onFlightRemove(item)
                }
                view.setOnLongClickListener {
                    onFlightSelect(layoutPosition, item)
                    true
                }
            }
        }
    }

    private fun getBgColor(
        item: Flight,
        context: Context
    ): Int = if (item.selected) {
        context.getIntColor(R.color.colorTextGrayBg)
    } else {
        val colorInt = item.colorInt
        if (colorInt == 0 || colorInt == -1 || colorInt == null) {
            context.getIntColor(R.color.colorTransparent)
        } else {
            colorInt
        }
    }
}
