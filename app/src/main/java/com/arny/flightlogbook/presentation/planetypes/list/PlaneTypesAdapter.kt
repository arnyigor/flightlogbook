package com.arny.flightlogbook.presentation.planetypes.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.databinding.PlaneTypeListItemLayoutBinding
import com.arny.flightlogbook.domain.models.PlaneType

class PlaneTypesAdapter(
    private val hideEdit: Boolean = false,
    private val typesListener: PlaneTypesListener? = null
) : SimpleAbstractAdapter<PlaneType>() {

    private lateinit var binding: PlaneTypeListItemLayoutBinding

    interface PlaneTypesListener : OnViewHolderListener<PlaneType> {
        fun onEditType(position: Int, item: PlaneType)
        fun onDeleteType(position: Int, item: PlaneType)
    }

    override fun getLayout(viewType: Int): Int = R.layout.plane_type_list_item_layout

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        binding = PlaneTypeListItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding.root)
    }

    override fun bindView(item: PlaneType, viewHolder: VH) {
        viewHolder.itemView.apply {
            val position = viewHolder.adapterPosition
            with(binding) {
                item.mainType?.let { tvTypeName.text = context.getString(it.nameRes) }
                tvTypeTitle.text = item.typeName
                tvRegNo.text = context.getString(
                    R.string.str_regnum_formatted,
                    item.regNo
                )
                ivTypeEdit.isVisible = !hideEdit
                ivTypeDelete.isVisible = !hideEdit
                ivTypeEdit.setOnClickListener {
                    typesListener?.onEditType(position, item)
                }
                ivTypeDelete.setOnClickListener {
                    typesListener?.onDeleteType(position, item)
                }
            }
            setOnClickListener {
                typesListener?.onItemClick(position, item)
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<PlaneType> {
        return object : DiffCallback<PlaneType>() {
            override fun areItemsTheSame(oldItem: PlaneType, newItem: PlaneType) =
                oldItem.typeId == newItem.typeId

            override fun areContentsTheSame(oldItem: PlaneType, newItem: PlaneType) =
                oldItem == newItem
        }
    }
}
