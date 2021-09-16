package com.arny.flightlogbook.presentation.flighttypes.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.arny.domain.models.FlightType
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.databinding.TypeListItemLayoutBinding

class FlightTypesAdapter(
    private val hideEdit: Boolean = false,
    private val typesListener: FlightTypesListener? = null
) : SimpleAbstractAdapter<FlightType>() {
    private lateinit var binding: TypeListItemLayoutBinding

    override fun getLayout(viewType: Int): Int {
        return R.layout.type_list_item_layout
    }

    interface FlightTypesListener : OnViewHolderListener<FlightType> {
        fun onEditType(position: Int, item: FlightType)
        fun onDeleteType(item: FlightType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        binding = TypeListItemLayoutBinding.inflate(inflater, parent, false)
        return VH(binding.root)
    }

    override fun bindView(item: FlightType, viewHolder: VH) {
        viewHolder.itemView.apply {
            val position = viewHolder.adapterPosition
            with(binding) {
                tvTypeTitle.text = item.typeTitle
                ivTypeEdit.isVisible = !hideEdit
                ivTypeDelete.isVisible = !hideEdit
                ivTypeEdit.setOnClickListener {
                    typesListener?.onEditType(position, item)
                }
                ivTypeDelete.setOnClickListener {
                    typesListener?.onDeleteType(item)
                }
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