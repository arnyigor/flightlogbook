package com.arny.flightlogbook.presentation.planetypes.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.diffUtilCallback
import com.arny.flightlogbook.databinding.PlaneTypeListItemLayoutBinding
import com.arny.flightlogbook.domain.models.PlaneType

class PlaneTypesAdapter(
    private val hideEdit: Boolean = false,
    val onEditType: (item: PlaneType) -> Unit,
    val onDeleteType: (item: PlaneType) -> Unit,
    val onItemClick: (item: PlaneType) -> Unit,
) : ListAdapter<PlaneType, PlaneTypesAdapter.PlaneTypesViewHolder>(diffUtilCallback<PlaneType>(
    areItemsTheSame = { old, new -> old.typeId == new.typeId },
    contentsTheSame = { old, new -> old == new }
)) {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): PlaneTypesViewHolder =
        PlaneTypesViewHolder(
            PlaneTypeListItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PlaneTypesViewHolder, position: Int) {
        holder.bind(getItem(position), hideEdit)
    }

  inner class PlaneTypesViewHolder constructor(
      private val viewBinding: PlaneTypeListItemLayoutBinding
    ) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: PlaneType, hideEdit: Boolean) {
            with(viewBinding) {
                val context = root.context
                item.mainType?.let { tvTypeName.text = context.getString(it.nameRes) }
                tvTypeTitle.text = item.typeName
                tvRegNo.text = context.getString(R.string.str_regnum_formatted, item.regNo)
                ivTypeEdit.isVisible = !hideEdit
                ivTypeDelete.isVisible = !hideEdit
                ivTypeEdit.setOnClickListener { onEditType(item) }
                ivTypeDelete.setOnClickListener { onDeleteType(item) }
                root.setOnClickListener { onItemClick(item) }
            }
        }
    }
}
