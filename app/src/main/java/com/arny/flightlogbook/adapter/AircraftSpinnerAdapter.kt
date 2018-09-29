package com.arny.flightlogbook.adapter

import android.content.Context
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.utils.adapters.AbstractArrayAdapter

class AircraftSpinnerAdapter(context: Context) : AbstractArrayAdapter<AircraftType>(context, android.R.layout.simple_list_item_1) {
    override fun getItemTitle(item: AircraftType): String {
        return item.typeName ?: ""
    }
}