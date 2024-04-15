package com.arny.flightlogbook.presentation.planetypes.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.PlaneType
import com.arny.flightlogbook.databinding.PlaneTypeListItemLayoutBinding
import com.arny.flightlogbook.presentation.utils.diffUtilCallback

class PlaneTypesAdapter(
    private val hideEdit: Boolean = false,
    val onEditType: (item: PlaneType) -> Unit,
    val onDeleteType: (item: PlaneType) -> Unit,
    val onItemClick: (item: PlaneType) -> Unit,
) : ListAdapter<PlaneType, PlaneTypesAdapter.PlaneTypesViewHolder>(
    diffUtilCallback<PlaneType>(itemsTheSame = { old, new -> old.typeId == new.typeId })
) {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): PlaneTypesViewHolder =
        PlaneTypesViewHolder(
            PlaneTypeListItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PlaneTypesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlaneTypesViewHolder(
        private val viewBinding: PlaneTypeListItemLayoutBinding
    ) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: PlaneType) {
            with(viewBinding) {
                val context = root.context
                item.mainType?.let { tvTypeName.text = context.getString(it.nameRes) }
                tvTypeTitle.text = item.typeName
                tvRegNo.text = context.getString(R.string.str_regnum_formatted, item.regNo)
                ivTypeDelete.isVisible = !hideEdit
                ivTypeEdit.setOnClickListener { onEditType(item) }
                ivTypeDelete.setOnClickListener { onDeleteType(item) }
                root.setOnClickListener { onItemClick(item) }
            }
        }
    }
}
