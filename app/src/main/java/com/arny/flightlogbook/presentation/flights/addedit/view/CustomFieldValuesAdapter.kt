package com.arny.flightlogbook.presentation.flights.addedit.view

import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import kotlinx.android.synthetic.main.custom_field_value_list_item.view.*

class CustomFieldValuesAdapter(private val onFieldChangeListener: OnFieldChangeListener? = null) :
        SimpleAbstractAdapter<CustomFieldValue>() {
    override fun getLayout(viewType: Int) = R.layout.custom_field_value_list_item

    interface OnFieldChangeListener {
        fun onValueChange(item: CustomFieldValue, value: String)
        fun onValueRemove(position: Int, item: CustomFieldValue)
    }

    override fun bindView(item: CustomFieldValue, viewHolder: VH) {
        val adapterPosition = viewHolder.adapterPosition
        viewHolder.itemView.apply {
            ivRemoveCustomFieldValue.isVisible = item.field?.showByDefault == false
            ivRemoveCustomFieldValue.setOnClickListener {
                onFieldChangeListener?.onValueRemove(adapterPosition, item)
            }
            val name = item.field?.name ?: ""
            val type = item.type ?: CustomFieldType.None
            tvTitleCustomField.isVisible = type != CustomFieldType.None
                    && type != CustomFieldType.Bool
                    && name.isNotBlank()
            tvTitleCustomField.text = name
            cfvView.init(type, name)
            val value = item.value
            if (value != null && value != "null") {
                cfvView.value = value
            }
            if (type == CustomFieldType.Bool) {
                cfvView.switch?.setOnCheckedChangeListener { _, isChecked ->
                    onFieldChangeListener?.onValueChange(item, isChecked.toString())
                }
            } else {
                val emptyHint = if (type is CustomFieldType.Time) context.getString(R.string.str_time_zero) else ""
                cfvView.editText?.let { editText ->
                    editText.setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) {
                            editText.setSelectAllOnFocus(false)
                            onFieldChangeListener?.onValueChange(item, editText.text.toString())
                        }
                        val flTime = editText.text.toString()
                        if (flTime.isBlank()) {
                            if (hasFocus) {
                                editText.hint = emptyHint
                            } else {
                                editText.hint = null
                            }
                        } else {
                            if (hasFocus && flTime == emptyHint) {
                                editText.setSelectAllOnFocus(true)
                                editText.selectAll()
                            }
                        }
                    }
                    editText.setOnEditorActionListener { _, actionId, _ ->
                        when (actionId) {
                            EditorInfo.IME_ACTION_NEXT -> {
                                onFieldChangeListener?.onValueChange(item, editText.text.toString())
                                true
                            }
                            else -> false
                        }
                    }
                }
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<CustomFieldValue>? {
        return object : DiffCallback<CustomFieldValue>() {
            override fun areItemsTheSame(oldItem: CustomFieldValue, newItem: CustomFieldValue): Boolean = oldItem == newItem

            override fun areContentsTheSame(oldItem: CustomFieldValue, newItem: CustomFieldValue): Boolean = oldItem == newItem
        }
    }
}
