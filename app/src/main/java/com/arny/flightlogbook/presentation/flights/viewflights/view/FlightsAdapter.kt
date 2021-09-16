package com.arny.flightlogbook.presentation.flights.viewflights.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.arny.core.utils.DateTimeUtils
import com.arny.domain.models.Flight
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.databinding.FlightListItemBinding

class FlightsAdapter(private val onFlightsListListener: OnFlightsListListener? = null) : SimpleAbstractAdapter<Flight>() {
    private lateinit var binding: FlightListItemBinding

    override fun getLayout(viewType: Int): Int {
        return R.layout.flight_list_item
    }

    interface OnFlightsListListener {
        fun onFlightSelect(position: Int, item: Flight)
        fun onFlightRemove(position: Int, item: Flight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        binding = FlightListItemBinding.inflate(inflater, parent, false)
        return VH(binding.root)
    }

    override fun bindView(item: Flight, viewHolder: VH) {
        val position = viewHolder.adapterPosition
        viewHolder.itemView.apply {
            with(binding){
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
                    ?: ContextCompat.getColor(context, R.color.colorTextPrimary)
                if (item.selected) {
                    colorText = ContextCompat.getColor(context, R.color.colorTextPrimary)
                    clFlightsItemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTextGrayBg))
                } else {
                    val colorInt = item.colorInt
                    if (colorInt == 0 || colorInt == -1 || colorInt == null) {
                        clFlightsItemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTransparent))
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
                setOnClickListener {
                    listener?.onItemClick(position, item)
                }
                ivRemove.setOnClickListener {
                    onFlightsListListener?.onFlightRemove(position, item)
                }
                setOnLongClickListener {
                    onFlightsListListener?.onFlightSelect(position, item)
                    true
                }
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<Flight>? {
        return object : DiffCallback<Flight>() {
            override fun areItemsTheSame(oldItem: Flight, newItem: Flight) = oldItem == newItem
            override fun areContentsTheSame(oldItem: Flight, newItem: Flight) = oldItem == newItem
        }
    }
}
