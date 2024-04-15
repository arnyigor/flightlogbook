package com.arny.flightlogbook.data.utils

import android.content.Context
import java.io.IOException
import java.io.InputStream

fun Context.readAssetFile(folder: String, fileName: String): String? {
    val input: InputStream
    try {
        input = assets.open("$folder/$fileName")
        val size = input.available()
        val buffer = ByteArray(size)
        input.read(buffer)
        input.close()
        return String(buffer)
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}