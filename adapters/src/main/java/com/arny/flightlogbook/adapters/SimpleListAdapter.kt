package com.arny.flightlogbook.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


abstract class SimpleListAdapter<T>(private var items: MutableList<T> = mutableListOf(), callback: DiffUtil
.ItemCallback<T>) :
        ListAdapter<T,
        SimpleListAdapter.VH>(callback) {
    protected var listener: OnViewHolderListener<T>? = null
    private val filter = ArrayFilter()
    private val lock = Any()
    protected abstract fun getLayout(viewType: Int = 0): Int
    protected abstract fun bindView(item: T, viewHolder: VH)
    private var onFilterObjectCallback: OnFilterObjectCallback? = null
    private var constraint: CharSequence? = ""

    interface OnViewHolderListener<T> {
        fun onItemClick(position: Int, item: T)
    }

    fun setViewHolderListener(listener: OnViewHolderListener<T>) {
        this.listener = listener
    }

    override fun onBindViewHolder(vh: VH, position: Int) {
        getItem(position)?.let { bindView(it, vh) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent, getLayout(viewType))
    }

    override fun getItemCount(): Int = currentList.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        constructor(parent: ViewGroup, @LayoutRes layout: Int) : this(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    fun addAll(list: List<T>) {
        items = list.toMutableList()
        submitList(items)
    }


    fun add(item: T) {
        try {
            items.add(item)
            submitList(items)
        } catch (e: Exception) {
        }
    }

    fun add(position: Int, item: T) {
        try {
            items.add(position, item)
            submitList(items)
        } catch (e: Exception) {
        }
    }

    fun remove(position: Int) {
        try {
            items.removeAt(position)
            submitList(items)
        } catch (e: Exception) {
        }
    }

    fun remove(item: T) {
        try {
            items.remove(item)
            submitList(items)
        } catch (e: Exception) {
        }
    }

    fun clear(notify: Boolean = false) {
        items.clear()
        submitList(items)
        if (notify) {
//            notifyDataSetChanged()
        }
    }

    fun setFilter(filter: SimpleAdapterFilter<T>): ArrayFilter {
        return this.filter.setFilter(filter)
    }

    interface SimpleAdapterFilter<T> {
        fun onFilterItem(contains: CharSequence, item: T): Boolean
    }

    fun convertResultToString(resultValue: Any): CharSequence {
        return filter.convertResultToString(resultValue)
    }

    fun filter(constraint: CharSequence) {
        this.constraint = constraint
        filter.filter(constraint)
    }

    fun filter(constraint: CharSequence, listener: Filter.FilterListener) {
        this.constraint = constraint
        filter.filter(constraint, listener)
    }

    protected fun itemToString(item: T): String? {
        return item.toString()
    }

    fun getFilter(): Filter {
        return filter
    }

    interface OnFilterObjectCallback {
        fun handle(countFilterObject: Int)
    }

    fun setOnFilterObjectCallback(objectCallback: OnFilterObjectCallback) {
        onFilterObjectCallback = objectCallback
    }

    inner class ArrayFilter : Filter() {
        private var original: MutableList<T> = mutableListOf()
        private var filter: SimpleAdapterFilter<T> = DefaultFilter()
        private var list: MutableList<T> = mutableListOf()
        private var values: MutableList<T> = mutableListOf()


        fun setFilter(filter: SimpleAdapterFilter<T>): ArrayFilter {
            original = items
            this.filter = filter
            return this
        }

        override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
            val results = Filter.FilterResults()
            if (constraint == null || constraint.isBlank()) {
                synchronized(lock) {
                    list = original
                }
                results.values = list
                results.count = list.size
            } else {
                synchronized(lock) {
                    values = original
                }
                val result = ArrayList<T>()
                for (value in values) {
                    if (!constraint.isNullOrBlank() && value != null) {
                        if (filter.onFilterItem(constraint, value)) {
                            result.add(value)
                        }
                    } else {
                        value?.let { result.add(it) }
                    }
                }
                results.values = result
                results.count = result.size
            }
            return results
        }

        override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
            items = results.values as? ArrayList<T> ?: arrayListOf()
            notifyDataSetChanged()
            onFilterObjectCallback?.handle(results.count)
        }

    }

    class DefaultFilter<T> : SimpleAdapterFilter<T> {
        override fun onFilterItem(contains: CharSequence, item: T): Boolean {
            val valueText = item.toString().toLowerCase()
            if (valueText.startsWith(contains.toString())) {
                return true
            } else {
                val words = valueText.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (word in words) {
                    if (word.contains(contains)) {
                        return true
                    }
                }
            }
            return false
        }
    }
}