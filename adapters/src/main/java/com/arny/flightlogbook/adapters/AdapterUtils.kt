package com.arny.flightlogbook.adapters

import androidx.recyclerview.widget.DiffUtil

fun <T : Any> diffUtilCallback(
    itemsTheSame: (old: T, new: T) -> Boolean = { old, new -> old == new },
    contentsTheSame: (old: T, new: T) -> Boolean = { old, new -> old == new },
): DiffUtil.ItemCallback<T> = object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = itemsTheSame(oldItem, newItem)

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
        contentsTheSame(oldItem, newItem)
}