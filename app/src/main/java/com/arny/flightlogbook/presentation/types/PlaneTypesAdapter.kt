package com.arny.flightlogbook.presentation.types

import com.arny.adapters.SimpleAbstractAdapter
import com.arny.domain.models.PlaneType
import com.arny.flightlogbook.R
import kotlinx.android.synthetic.main.typeitem.view.*

class PlaneTypesAdapter(private val flightTypesListener: FlightTypesListener? = null) : SimpleAbstractAdapter<PlaneType>() {

    interface FlightTypesListener : OnViewHolderListener<PlaneType> {
        fun onTypeEdit(position: Int, item: PlaneType)
        fun onTypeDelete(position: Int, item: PlaneType)
    }

    override fun getLayout(viewType: Int): Int {
        return R.layout.typeitem
    }

    override fun bindView(item: PlaneType, viewHolder: VH) {
        viewHolder.itemView.apply {
            val position = viewHolder.adapterPosition
            tv_plane_title.text = item.typeName
            iv_edit_plane_type_title.setOnClickListener {
                flightTypesListener?.onTypeEdit(position, item)
            }
            iv_delete_plane_type_title.setOnClickListener {
                flightTypesListener?.onTypeDelete(position, item)
            }
            setOnClickListener {
                flightTypesListener?.onItemClick(position, item)
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<PlaneType>? {
        return object : DiffCallback<PlaneType>() {
            override fun areItemsTheSame(oldItem: PlaneType, newItem: PlaneType): Boolean {
                return oldItem.typeId == newItem.typeId
            }

            override fun areContentsTheSame(oldItem: PlaneType, newItem: PlaneType): Boolean {
                return oldItem.typeId == newItem.typeId && oldItem.typeName == newItem.typeName
            }
        }
    }

}