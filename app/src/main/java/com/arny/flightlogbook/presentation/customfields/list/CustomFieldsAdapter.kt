package com.arny.flightlogbook.presentation.customfields.list

import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.customfields.models.CustomField
import kotlinx.android.synthetic.main.custom_field_list_item.view.*

class CustomFieldsAdapter : SimpleAbstractAdapter<CustomField>() {

    override fun getLayout(viewType: Int) = R.layout.custom_field_list_item

    override fun bindView(item: CustomField, viewHolder: VH) {
        val position = viewHolder.adapterPosition
        viewHolder.itemView.apply {
            tvFieldName.text = item.name
            val type = item.type
            tvFieldTypeName.text = context.getString(type.nameRes)
            tvFieldDescription.text = context.getString(type.descRes)
            setOnClickListener {
                listener?.onItemClick(position, item)
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<CustomField>? {
        return object : DiffCallback<CustomField>() {
            override fun areItemsTheSame(oldItem: CustomField, newItem: CustomField): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: CustomField, newItem: CustomField): Boolean {
                return oldItem == newItem
            }
        }
    }
}
