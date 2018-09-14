package com.arny.flightlogbook.utils

import android.content.Context

import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable

@JvmOverloads
fun getGMDIcon(context: Context, gmd_icon: GoogleMaterial.Icon, size: Int, color: Int? = null): IconicsDrawable? {
    val icon = IconicsDrawable(context).icon(gmd_icon)
    icon.sizeDp(size)
    if (color != null) {
        icon.color(color)
    }
    return icon
}
