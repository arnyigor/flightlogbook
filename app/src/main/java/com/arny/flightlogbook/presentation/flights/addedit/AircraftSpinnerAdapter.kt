package com.arny.flightlogbook.presentation.flights.addedit

import android.content.Context
import com.arny.adapters.AbstractArrayAdapter
import com.arny.domain.models.PlaneType

class AircraftSpinnerAdapter(context: Context) : AbstractArrayAdapter<PlaneType>(context, android.R.layout.simple_list_item_1) {
    override fun getItemTitle(item: PlaneType): String {
        return item.typeName ?: ""
    }
}