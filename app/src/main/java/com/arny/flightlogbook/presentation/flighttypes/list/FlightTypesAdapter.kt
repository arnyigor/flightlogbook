package com.arny.flightlogbook.presentation.flighttypes.list

import androidx.core.view.isVisible
import com.arny.domain.models.FlightType
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import kotlinx.android.synthetic.main.type_list_item_layout.view.*

class FlightTypesAdapter(
        private val hideEdit: Boolean = false,
        private val typesListener: FlightTypesListener? = null
) : SimpleAbstractAdapter<FlightType>() {
    override fun getLayout(viewType: Int): Int {
        return R.layout.type_list_item_layout
    }

    interface FlightTypesListener : OnViewHolderListener<FlightType> {
        fun onEditType(position: Int, item: FlightType)
        fun onDeleteType(item: FlightType)
    }

    override fun bindView(item: FlightType, viewHolder: VH) {
        viewHolder.itemView.apply {
            val position = viewHolder.adapterPosition
            tvTypeTitle.text = item.typeTitle
            iv_type_edit.isVisible = !hideEdit
            iv_type_delete.isVisible = !hideEdit
            iv_type_edit.setOnClickListener {
                typesListener?.onEditType(position, item)
            }
            iv_type_delete.setOnClickListener {
                typesListener?.onDeleteType(item)
            }
            setOnClickListener {
                typesListener?.onItemClick(position, item)
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