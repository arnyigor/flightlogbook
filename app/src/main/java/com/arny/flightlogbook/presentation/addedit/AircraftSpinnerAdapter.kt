package com.arny.flightlogbook.presentation.addedit

import android.content.Context
import com.arny.flightlogbook.data.models.PlaneType
import com.arny.flightlogbook.utils.adapters.AbstractArrayAdapter

class AircraftSpinnerAdapter(context: Context) : AbstractArrayAdapter<PlaneType>(context, android.R.layout.simple_list_item_1) {
    override fun getItemTitle(item: PlaneType): String {
        return item.typeName ?: ""
    }
}