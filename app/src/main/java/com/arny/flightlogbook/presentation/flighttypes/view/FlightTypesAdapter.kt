package com.arny.flightlogbook.presentation.flighttypes.view

import com.arny.adapters.SimpleAbstractAdapter
import com.arny.domain.models.FlightType
import com.arny.flightlogbook.R
import com.arny.helpers.utils.setVisible
import kotlinx.android.synthetic.main.type_list_item_layout.view.*

class FlightTypesAdapter(private val typesListener: FlightTypesListener? = null, private val hideEdit: Boolean = false) : SimpleAbstractAdapter<FlightType>() {
    override fun getLayout(viewType: Int): Int {
        return R.layout.type_list_item_layout
    }

    interface FlightTypesListener {
        fun onEditType(position: Int, item: FlightType)
        fun onDeleteType(item: FlightType)
    }

    override fun bindView(item: FlightType, viewHolder: VH) {
        viewHolder.itemView.apply {
            val position = viewHolder.adapterPosition
            tv_type_title.text = item.typeTitle
            iv_type_edit.setVisible(!hideEdit)
            iv_type_delete.setVisible(!hideEdit)
            iv_type_edit.setOnClickListener {
                typesListener?.onEditType(position, item)
            }
            iv_type_delete.setOnClickListener {
                typesListener?.onDeleteType(item)
            }
            setOnClickListener {
                listener?.onItemClick(position, item)
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<FlightType>? {
        return object : DiffCallback<FlightType>() {
            override fun areItemsTheSame(oldItem: FlightType, newItem: FlightType): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: FlightType, newItem: FlightType): Boolean {
                return oldItem == newItem
            }
        }
    }
}