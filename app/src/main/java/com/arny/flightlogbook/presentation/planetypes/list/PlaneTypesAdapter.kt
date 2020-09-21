package com.arny.flightlogbook.presentation.planetypes.list

import com.arny.domain.models.PlaneType
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.helpers.utils.setVisible
import kotlinx.android.synthetic.main.plane_type_list_item_layout.view.*

class PlaneTypesAdapter(private val typesListener: PlaneTypesListener? = null, private val hideEdit: Boolean = false) : SimpleAbstractAdapter<PlaneType>() {

    interface PlaneTypesListener : OnViewHolderListener<PlaneType> {
        fun onEditType(position: Int, item: PlaneType)
        fun onDeleteType(position: Int, item: PlaneType)
    }

    override fun getLayout(viewType: Int): Int = R.layout.plane_type_list_item_layout

    override fun bindView(item: PlaneType, viewHolder: VH) {
        viewHolder.itemView.apply {
            val position = viewHolder.adapterPosition
            tvTypeTitle.text = item.typeName
            tvRegNo.text = item.regNo
            iv_type_edit.setVisible(!hideEdit)
            iv_type_delete.setVisible(!hideEdit)
            iv_type_edit.setOnClickListener {
                typesListener?.onEditType(position, item)
            }
            iv_type_delete.setOnClickListener {
                typesListener?.onDeleteType(position,item)
            }
            setOnClickListener {
                typesListener?.onItemClick(position, item)
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
