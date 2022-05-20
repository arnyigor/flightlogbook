package com.arny.flightlogbook.adapters

import androidx.recyclerview.widget.DiffUtil

fun <T : Any> diffUtilCallback(
    areItemsTheSame: (old: T, new: T) -> Boolean,
    contentsTheSame: (old: T, new: T) -> Boolean
): DiffUtil.ItemCallback<T> = object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
        areItemsTheSame(oldItem, newItem)

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
        contentsTheSame(oldItem, newItem)
}