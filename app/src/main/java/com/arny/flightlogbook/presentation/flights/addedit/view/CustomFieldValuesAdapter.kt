package com.arny.flightlogbook.presentation.flights.addedit.view

import androidx.core.view.isVisible
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import kotlinx.android.synthetic.main.custom_field_value_list_item.view.*

class CustomFieldValuesAdapter : SimpleAbstractAdapter<CustomFieldValue>() {
    override fun getLayout(viewType: Int) = R.layout.custom_field_value_list_item

    override fun bindView(item: CustomFieldValue, viewHolder: VH) {

        viewHolder.itemView.apply {
            val name = item.field?.name ?: ""
            val type = item.type ?: CustomFieldType.TYPE_NONE
            tvTitleCustomField.isVisible = type != CustomFieldType.TYPE_NONE
                    && type != CustomFieldType.TYPE_BOOLEAN
                    && name.isNotBlank()
            tvTitleCustomField.text = name
            cfvView.init(type, name)
        }
    }
}
