package com.arny.flightlogbook.presentation.customfields.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.diffUtilCallback
import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.databinding.CustomFieldListItemBinding
import java.util.*

class CustomFieldsAdapter(
    val onItemClick: (item: CustomField) -> Unit
) : ListAdapter<CustomField, CustomFieldsAdapter.CustomFieldsViewHolder>(
    diffUtilCallback<CustomField>(itemsTheSame = { old, new -> old.id == new.id },)
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomFieldsViewHolder =
        CustomFieldsViewHolder(
            CustomFieldListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: CustomFieldsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CustomFieldsViewHolder(
        private val binding: CustomFieldListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CustomField) {
            val root = binding.root
            val context = root.context
            var addTimeName = ""
            val type = item.type
            if (type is CustomFieldType.Time && item.addTime) {
                addTimeName = context.getString(R.string.str_time_add_main)
            }
            var showDefault = ""
            if (item.showByDefault) {
                showDefault = context.getString(R.string.custom_field_name_default)
            }
            binding.tvFieldName.text = String.format(
                Locale.getDefault(),
                "%s%s%s",
                item.name,
                addTimeName,
                showDefault
            )
            binding.tvFieldTypeName.text = context.getString(type.nameRes)
            binding.tvFieldDescription.text = context.getString(type.descRes)
            root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
