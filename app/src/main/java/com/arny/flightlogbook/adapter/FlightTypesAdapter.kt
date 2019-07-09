package com.arny.flightlogbook.adapter

import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.utils.adapters.SimpleAbstractAdapter
import kotlinx.android.synthetic.main.typeitem.view.*

class FlightTypesAdapter(private val flightTypesListener: FlightTypesListener? = null) : SimpleAbstractAdapter<AircraftType>() {

    interface FlightTypesListener : OnViewHolderListener<AircraftType> {
        fun onTypeEdit(position: Int, item: AircraftType)

        fun onTypeDelete(position: Int, item: AircraftType)
    }

    override fun getLayout(viewType: Int): Int {
        return R.layout.typeitem
    }

    override fun bindView(item: AircraftType, viewHolder: VH) {
        viewHolder.itemView.apply {
            val position = viewHolder.adapterPosition
            typeTitle.text = item.typeName
            edit.setOnClickListener {
                flightTypesListener?.onTypeEdit(position, item)
            }
            delete.setOnClickListener {
                flightTypesListener?.onTypeDelete(position, item)
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<AircraftType>? {
        return object : DiffCallback<AircraftType>() {
            override fun areItemsTheSame(oldItem: AircraftType, newItem: AircraftType): Boolean {
                return oldItem.typeId == oldItem.typeId
            }

            override fun areContentsTheSame(oldItem: AircraftType, newItem: AircraftType): Boolean {
                return oldItem.typeName == oldItem.typeName
            }
        }
    }

}
