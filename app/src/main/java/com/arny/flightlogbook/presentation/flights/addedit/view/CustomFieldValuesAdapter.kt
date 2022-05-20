package com.arny.flightlogbook.presentation.flights.addedit.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.diffUtilCallback
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.databinding.CustomFieldValueListItemBinding
import java.util.*

class CustomFieldValuesAdapter(
    private val onValueChange: (item: CustomFieldValue, value: String) -> Unit,
    private val onValueRemove: (position: Int) -> Unit
) : ListAdapter<CustomFieldValue, CustomFieldValuesAdapter.AdapterViewHolder>(
    diffUtilCallback<CustomFieldValue>(
        areItemsTheSame = { old, new -> old.id == new.id },
        contentsTheSame = { old, new -> old == new }
    )
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder =
        AdapterViewHolder(
            CustomFieldValueListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AdapterViewHolder(
        private val binding: CustomFieldValueListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CustomFieldValue) {
            val context = binding.root.context
            binding.ivRemoveCustomFieldValue.isVisible = item.field?.showByDefault == false

            binding.ivRemoveCustomFieldValue.setOnClickListener {
                onValueRemove(layoutPosition)
            }
            val type = item.type ?: CustomFieldType.None
            var addTimeName = ""
            if (type is CustomFieldType.Time && type.addTime) {
                addTimeName = context.getString(R.string.str_time_add_main)
            }
            val name = item.field?.name.orEmpty()
            binding.tvTitleCustomField.isVisible = type != CustomFieldType.None
                    && type != CustomFieldType.Bool
                    && name.isNotBlank()
            binding.tvTitleCustomField.text = String.format(
                Locale.getDefault(),
                "%s%s",
                name,
                addTimeName
            )
            binding.cfvView.init(type, name)
            val value = item.value
            if (value != null && value != "null") {
                binding.cfvView.value = value
            }
            if (type == CustomFieldType.Bool) {
                binding.cfvView.switch?.setOnCheckedChangeListener { _, isChecked ->
                    onValueChange(item, isChecked.toString())
                }
            } else {
                val emptyHint =
                    if (type is CustomFieldType.Time) {
                        context.getString(R.string.str_time_zero)
                    } else {
                        ""
                    }
                binding.cfvView.editText?.let { editText ->
                    editText.doAfterTextChanged {
                        if (editText.hasFocus()) {
                            onValueChange(item, editText.text.toString())
                        }
                    }
                    editText.setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) {
                            editText.setSelectAllOnFocus(false)
                            onValueChange(item, editText.text.toString())
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
                                onValueChange(item, editText.text.toString())
                                true
                            }
                            else -> false
                        }
                    }
                }
            }
        }
    }
}
