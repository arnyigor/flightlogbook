package com.arny.flightlogbook.presentation.flights.addedit.view

import android.content.Context
import com.arny.flightlogbook.adapters.AbstractArrayAdapter

class MultiAutoCompleteAdapter(
        context: Context
) : AbstractArrayAdapter<String>(
        context,
        android.R.layout.simple_dropdown_item_1line
) {

    override fun getItemTitle(item: String?): String {
        return item.toString()
    }

}
