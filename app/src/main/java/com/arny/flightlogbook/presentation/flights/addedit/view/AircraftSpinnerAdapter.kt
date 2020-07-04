package com.arny.flightlogbook.presentation.flights.addedit.view

import android.content.Context
import com.arny.domain.models.PlaneType
import com.arny.flightlogbook.adapters.AbstractArrayAdapter

class AircraftSpinnerAdapter(context: Context) : AbstractArrayAdapter<PlaneType>(context, android.R.layout.simple_list_item_1) {
    override fun getItemTitle(item: PlaneType): String {
        return item.typeName ?: ""
    }
}