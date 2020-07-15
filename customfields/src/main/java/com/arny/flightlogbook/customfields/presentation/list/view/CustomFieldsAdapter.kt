package com.arny.flightlogbook.customfields.presentation.list.view

import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.customfields.R
import com.arny.flightlogbook.customfields.models.CustomField
import kotlinx.android.synthetic.main.custom_field_list_item.view.*

class CustomFieldsAdapter : SimpleAbstractAdapter<CustomField>() {

    override fun getLayout(viewType: Int) = R.layout.custom_field_list_item

    override fun bindView(item: CustomField, viewHolder: VH) {
        val position = viewHolder.adapterPosition
        viewHolder.itemView.apply {
            tvFieldName.text = item.name
            tvFieldTypeName.text = context.getString(item.type.getTypeName())
            tvFieldDescription.text = context.getString(item.type.getTypeDescr())
            setOnClickListener {
                listener?.onItemClick(position, item)
            }
        }
    }
}
