package com.arny.flightlogbook.presentation.flights.addedit.view

import android.content.Context
import com.arny.flightlogbook.adapters.AbstractArrayAdapter
import com.arny.flightlogbook.domain.models.PlaneType

class AircraftSpinnerAdapter(context: Context) : AbstractArrayAdapter<PlaneType>(context, android.R.layout.simple_list_item_1) {
    override fun getItemTitle(item: PlaneType): String {
        return item.typeName ?: ""
    }
}