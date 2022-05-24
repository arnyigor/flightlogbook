package com.arny.flightlogbook.presentation.customfields.list

import android.view.LayoutInflater
import android.view.ViewGroup
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.databinding.CustomFieldListItemBinding

class CustomFieldsAdapter : SimpleAbstractAdapter<CustomField>() {

    private lateinit var binding: CustomFieldListItemBinding

    override fun getLayout(viewType: Int) = R.layout.custom_field_list_item

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        binding = CustomFieldListItemBinding.inflate(inflater, parent, false)
        return VH(binding.root)
    }

    override fun bindView(item: CustomField, viewHolder: VH) {
        val position = viewHolder.layoutPosition
        viewHolder.itemView.apply {
            binding.tvFieldName.text = item.name
            val type = item.type
            binding.tvFieldTypeName.text = context.getString(type.nameRes)
            binding.tvFieldDescription.text = context.getString(type.descRes)
            setOnClickListener {
                listener?.onItemClick(position, item)
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<CustomField> =
        object : DiffCallback<CustomField>() {
            override fun areItemsTheSame(oldItem: CustomField, newItem: CustomField): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: CustomField, newItem: CustomField): Boolean {
                return oldItem == newItem
            }
        }
}
