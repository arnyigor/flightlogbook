package com.arny.flightlogbook.presentation.timetypes

import com.arny.adapters.SimpleAbstractAdapter
import com.arny.domain.models.TimeType
import com.arny.flightlogbook.R
import com.arny.helpers.utils.setVisible
import kotlinx.android.synthetic.main.type_list_item_layout.view.*

class TimeTypesAdapter( private val timeTypesListener: TimeTypesListener? = null,private val hideEdit:Boolean = false) : SimpleAbstractAdapter<TimeType>() {
    override fun getLayout(viewType: Int): Int {
        return R.layout.type_list_item_layout
    }

    interface TimeTypesListener : OnViewHolderListener<TimeType> {
        fun onEditTimeType(position: Int, item: TimeType)
        fun onDeleteTimeType(item: TimeType)
    }

    override fun bindView(item: TimeType, viewHolder: VH) {
        viewHolder.itemView.apply {
            val position = viewHolder.adapterPosition
            tv_type_title.text = item.title
            iv_type_edit.setVisible(!hideEdit)
            iv_type_delete.setVisible(!hideEdit)
            iv_type_edit.setOnClickListener {
                timeTypesListener?.onEditTimeType(position, item)
            }
            iv_type_delete.setOnClickListener {
                timeTypesListener?.onDeleteTimeType(item)
            }
            setOnClickListener {
                timeTypesListener?.onItemClick(position, item)
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<TimeType>? {
        return object : DiffCallback<TimeType>() {
            override fun areItemsTheSame(oldItem: TimeType, newItem: TimeType): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: TimeType, newItem: TimeType): Boolean {
                return oldItem == newItem
            }
        }
    }
}