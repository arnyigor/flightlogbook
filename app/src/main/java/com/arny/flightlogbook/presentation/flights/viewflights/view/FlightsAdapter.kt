package com.arny.flightlogbook.presentation.flights.viewflights.view

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.arny.domain.models.Flight
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.helpers.utils.DateTimeUtils
import kotlinx.android.synthetic.main.flight_list_item.view.*

class FlightsAdapter : SimpleAbstractAdapter<Flight>() {
    override fun getLayout(viewType: Int): Int {
        return R.layout.flight_list_item
    }

    override fun bindView(item: Flight, viewHolder: VH) {
        val position = viewHolder.adapterPosition
        viewHolder.itemView.apply {
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
            val colorText = item.colorText
                    ?: ContextCompat.getColor(context, R.color.colorTextPrimary)
            colorText.let {
                tvLogTimeFlightTotal.setTextColor(it)
                tvLogTimeFlight.setTextColor(it)
                tvDate.setTextColor(it)
                tvPlaneRegNo.setTextColor(it)
                tvPlaneType.setTextColor(it)
                tvDescr.setTextColor(it)
                tvFlightType.setTextColor(it)
            }
            val colorInt = item.colorInt
            if (colorInt == 0 || colorInt == -1 || colorInt == null) {
                clFlightsItemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTransparent))
            } else {
                clFlightsItemContainer.setBackgroundColor(colorInt)
            }
            setOnClickListener {
                listener?.onItemClick(position, item)
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
