package com.arny.flightlogbook.presentation.flights.viewflights.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arny.core.utils.DateTimeUtils
import com.arny.core.utils.getIntColor
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.FlightListItemBinding
import com.arny.flightlogbook.domain.models.Flight

class FlightsAdapter(private val onFlightsListListener: OnFlightsListListener? = null) :
    ListAdapter<Flight, FlightsAdapter.AdapterViewholder>(
        object : DiffUtil.ItemCallback<Flight>() {
            override fun areItemsTheSame(
                oldItem: Flight,
                newItem: Flight
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: Flight,
                newItem: Flight
            ): Boolean = oldItem == newItem
        }
    ) {

    interface OnFlightsListListener {
        fun onItemClick(position: Int, item: Flight)
        fun onFlightSelect(position: Int, item: Flight)
        fun onFlightRemove(position: Int, item: Flight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewholder {
        return AdapterViewholder(
            FlightListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: AdapterViewholder, position: Int) {
        holder.bind(getItem(holder.adapterPosition))
    }

    inner class AdapterViewholder(private val binding: FlightListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Flight) {
            val view = binding.root
            val context = view.context
            with(binding) {
                val datetime = item.datetime ?: 0
                if (datetime > 0) {
                    tvDate.text = item.datetimeFormatted
                }
                tvLogTimeFlight.text = item.logtimeFormatted
                tvLogTimeFlightTotal.text = DateTimeUtils.strLogTime(item.totalTime)
                tvFlightType.text = item.flightType?.typeTitle
                tvPlaneRegNo.text = item.regNo
                tvPlaneType.text = item.planeType?.typeName
                tvDescr.isVisible = !item.description.isNullOrBlank()
                tvDescr.text = item.description
                var colorText = item.colorText
                    ?: context.getIntColor(R.color.colorTextPrimary)
                if (item.selected) {
                    colorText = ContextCompat.getColor(context, R.color.colorTextPrimary)
                    clFlightsItemContainer.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.colorTextGrayBg
                        )
                    )
                } else {
                    val colorInt = item.colorInt
                    if (colorInt == 0 || colorInt == -1 || colorInt == null) {
                        clFlightsItemContainer.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.colorTransparent
                            )
                        )
                    } else {
                        clFlightsItemContainer.setBackgroundColor(colorInt)
                    }
                }
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
                    onFlightsListListener?.onItemClick(adapterPosition, item)
                }
                ivRemove.setOnClickListener {
                    onFlightsListListener?.onFlightRemove(adapterPosition, item)
                }
                view.setOnLongClickListener {
                    onFlightsListListener?.onFlightSelect(adapterPosition, item)
                    true
                }
            }
        }
    }
}
