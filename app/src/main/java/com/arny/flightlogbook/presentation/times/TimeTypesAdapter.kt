package com.arny.flightlogbook.presentation.times

import com.arny.adapters.SimpleAbstractAdapter
import com.arny.data.db.intities.TimeTypeEntity
import com.arny.flightlogbook.R
import com.arny.helpers.utils.getIntColor
import com.arny.helpers.utils.setSrcTintColor
import kotlinx.android.synthetic.main.time_type_list_item_layout.view.*

class TimeTypesAdapter(private val timeTypesListener: TimeTypesListener? = null) : SimpleAbstractAdapter<TimeTypeEntity>() {
    override fun getLayout(viewType: Int): Int {
        return R.layout.time_type_list_item_layout
    }

    interface TimeTypesListener : OnViewHolderListener<TimeTypeEntity> {
        fun onEditTimeType(position: Int, item: TimeTypeEntity)
        fun onDeleteTimeType(item: TimeTypeEntity)
    }

    override fun bindView(item: TimeTypeEntity, viewHolder: VH) {
        viewHolder.itemView.apply {
            val position = viewHolder.adapterPosition
            if (item.selected) {
                cl_time_type_container.setBackgroundColor(context.getIntColor(R.color.colorPrimary))
                tv_time_type_title.setTextColor(context.getIntColor(R.color.colorTextWhite))
                iv_time_type_edit.setSrcTintColor(R.drawable.ic_icon_edit, context.getIntColor(R.color.colorTextWhite))
                iv_time_type_delete.setSrcTintColor(R.drawable.ic_icon_remove, context.getIntColor(R.color.colorTextWhite))
            } else {
                cl_time_type_container.setBackgroundColor(context.getIntColor(R.color.colorTextWhite))
                tv_time_type_title.setTextColor(context.getIntColor(R.color.colorTextSecondary))
                iv_time_type_edit.setSrcTintColor(R.drawable.ic_icon_edit, context.getIntColor(R.color.colorTextSecondary))
                iv_time_type_delete.setSrcTintColor(R.drawable.ic_icon_remove, context.getIntColor(R.color.colorTextSecondary))
            }
            tv_time_type_title.text = item.title
            iv_time_type_edit.setOnClickListener {
                timeTypesListener?.onEditTimeType(position, item)
            }
            iv_time_type_delete.setOnClickListener {
                timeTypesListener?.onDeleteTimeType(item)
            }
            setOnClickListener {
                timeTypesListener?.onItemClick(position, item)
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<TimeTypeEntity>? {
        return object : DiffCallback<TimeTypeEntity>() {
            override fun areItemsTheSame(oldItem: TimeTypeEntity, newItem: TimeTypeEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: TimeTypeEntity, newItem: TimeTypeEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}