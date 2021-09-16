package com.arny.flightlogbook.presentation.flights.addedit.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.databinding.CustomFieldValueListItemBinding

class CustomFieldValuesAdapter(private val onFieldChangeListener: OnFieldChangeListener? = null) :
    SimpleAbstractAdapter<CustomFieldValue>() {
    private lateinit var binding: CustomFieldValueListItemBinding

    override fun getLayout(viewType: Int) = R.layout.custom_field_value_list_item

    interface OnFieldChangeListener {
        fun onValueChange(item: CustomFieldValue, value: String)
        fun onValueRemove(position: Int, item: CustomFieldValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        binding = CustomFieldValueListItemBinding.inflate(inflater, parent, false)
        return VH(binding.root)
    }

    override fun bindView(item: CustomFieldValue, viewHolder: VH) {
        val adapterPosition = viewHolder.adapterPosition
        viewHolder.itemView.apply {
            binding.ivRemoveCustomFieldValue.isVisible = item.field?.showByDefault == false
            binding.ivRemoveCustomFieldValue.setOnClickListener {
                onFieldChangeListener?.onValueRemove(adapterPosition, item)
            }
            val name = item.field?.name ?: ""
            val type = item.type ?: CustomFieldType.None
            binding.tvTitleCustomField.isVisible = type != CustomFieldType.None
                    && type != CustomFieldType.Bool
                    && name.isNotBlank()
            binding.tvTitleCustomField.text = name
            binding.cfvView.init(type, name)
            val value = item.value
            if (value != null && value != "null") {
                binding.cfvView.value = value
            }
            if (type == CustomFieldType.Bool) {
                binding.cfvView.switch?.setOnCheckedChangeListener { _, isChecked ->
                    onFieldChangeListener?.onValueChange(item, isChecked.toString())
                }
            } else {
                val emptyHint =
                    if (type is CustomFieldType.Time) context.getString(R.string.str_time_zero) else ""
                binding.cfvView.editText?.let { editText ->
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

    override fun getDiffCallback(): DiffCallback<CustomFieldValue> {
        return object : DiffCallback<CustomFieldValue>() {
            override fun areItemsTheSame(
                oldItem: CustomFieldValue,
                newItem: CustomFieldValue
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: CustomFieldValue,
                newItem: CustomFieldValue
            ): Boolean = oldItem == newItem
        }
    }
}
