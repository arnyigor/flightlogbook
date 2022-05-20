package com.arny.flightlogbook.presentation.flighttypes.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arny.flightlogbook.adapters.diffUtilCallback
import com.arny.flightlogbook.databinding.TypeListItemLayoutBinding
import com.arny.flightlogbook.domain.models.FlightType

class FlightTypesAdapter(
    private val hideEdit: Boolean = false,
    val onEditType: (item: FlightType) -> Unit,
    val onDeleteType: (item: FlightType) -> Unit,
    val onItemClick: (item: FlightType) -> Unit,
) : ListAdapter<FlightType, FlightTypesAdapter.FlightTypesViewHolder>(diffUtilCallback<FlightType>(
    areItemsTheSame = { old, new -> old.id == new.id },
    contentsTheSame = { old, new -> old == new }
)) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): FlightTypesViewHolder =
        FlightTypesViewHolder(
            TypeListItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: FlightTypesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FlightTypesViewHolder constructor(
        private val viewBinding: TypeListItemLayoutBinding
    ) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: FlightType) {
            with(viewBinding) {
                tvTypeTitle.text = item.typeTitle
                ivTypeEdit.isVisible = !hideEdit
                ivTypeDelete.isVisible = !hideEdit
                ivTypeEdit.setOnClickListener {
                    onEditType(item)
                }
                ivTypeDelete.setOnClickListener {
                    onDeleteType(item)
                }
            }
            viewBinding.root.setOnClickListener {
                onItemClick(item)
            }
        }

    }
}