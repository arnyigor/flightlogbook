package com.arny.flightlogbook.presenter.viewflights

import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.utils.DateTimeUtils
import com.arny.flightlogbook.utils.adapters.SimpleAbstractAdapter
import kotlinx.android.synthetic.main.flight_list_item.view.*

class FlightsAdapter: SimpleAbstractAdapter<Flight>() {
    override fun getLayout(viewType: Int): Int {
        return R.layout.flight_list_item
    }

    override fun bindView(item: Flight, viewHolder: VH) {
        viewHolder.itemView.apply {
            val datetime = item.datetime ?: 0
            if (datetime > 0) {
                tvDate.text = DateTimeUtils.getDateTime(datetime, "dd MMM yyyy")
            }
            val logtime = item.logtime ?: 0
            tvLogTime.text = DateTimeUtils.strLogTime(logtime)
            tvType.text = item.airplanetypetitle
            setOnClickListener {
                listener?.onItemClick(viewHolder.adapterPosition, item)
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<Flight>? {
        return object : DiffCallback<Flight>() {
            override fun areItemsTheSame(oldItem: Flight, newItem: Flight): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Flight, newItem: Flight): Boolean {
                val equals = oldItem.logtime == newItem.logtime && oldItem.datetime == newItem.datetime && oldItem.aircraft_id == newItem.aircraft_id
                return equals
            }
        }
    }

}
