package com.arny.flightlogbook.presentation.planetypes.list

import androidx.core.view.isVisible
import com.arny.domain.models.PlaneType
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import kotlinx.android.synthetic.main.plane_type_list_item_layout.view.*

class PlaneTypesAdapter(
    private val hideEdit: Boolean = false,
    private val typesListener: PlaneTypesListener? = null
) : SimpleAbstractAdapter<PlaneType>() {

    interface PlaneTypesListener : OnViewHolderListener<PlaneType> {
        fun onEditType(position: Int, item: PlaneType)
        fun onDeleteType(position: Int, item: PlaneType)
    }

    override fun getLayout(viewType: Int): Int = R.layout.plane_type_list_item_layout

    override fun bindView(item: PlaneType, viewHolder: VH) {
        viewHolder.itemView.apply {
            val position = viewHolder.adapterPosition
            item.mainType?.let { tvTypeName.text = context.getString(it.nameRes) }
            tvTypeTitle.text = item.typeName
            tvRegNo.text = context.getString(
                R.string.str_regnum_formatted,
                item.regNo
            )
            iv_type_edit.isVisible =!hideEdit
            iv_type_delete.isVisible = !hideEdit
            iv_type_edit.setOnClickListener {
                typesListener?.onEditType(position, item)
            }
            iv_type_delete.setOnClickListener {
                typesListener?.onDeleteType(position, item)
            }
            setOnClickListener {
                typesListener?.onItemClick(position, item)
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<PlaneType>? {
        return object : DiffCallback<PlaneType>() {
            override fun areItemsTheSame(oldItem: PlaneType, newItem: PlaneType) =
                oldItem.typeId == newItem.typeId

            override fun areContentsTheSame(oldItem: PlaneType, newItem: PlaneType) =
                oldItem == newItem
        }
    }
}
