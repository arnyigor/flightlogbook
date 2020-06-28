package com.arny.helpers.utils

import android.graphics.Color
import kotlin.math.sqrt

private val COLORS_LIST = listOf(
        "#000000",
        "#FF0000",
        "#FFDB66",
        "#006401",
        "#010067",
        "#95003A",
        "#007DB5",
        "#774D00",
        "#FF937E",
        "#6A826C",
        "#A42400",
        "#00AE7E",
        "#683D3B",
        "#001544",
        "#01D0FF",
        "#004754",
        "#0E4CA1",
        "#91D0CB",
        "#FFE502",
        "#FF6E41",
        "#6B6882",
        "#009BFF"
)

fun getColorsIntArray(): IntArray {
    return COLORS_LIST.toIntColorsArray()
}

fun List<String>.toIntColorsArray(): IntArray {
    return this
            .map { it.toIntColor() }
            .toIntArray()
}

fun colorWillBeMasked(color: Int): Boolean {
    if (android.R.color.transparent == color) return true
    val rgb = intArrayOf(
            Color.red(color),
            Color.green(color),
            Color.blue(color)
    )
    return sqrt(
            rgb[0] * rgb[0] * .241 + rgb[1] * rgb[1] * .691 + rgb[2] * rgb[2] * .068
    ).toInt() <= 144
}

fun Int.toHexColor(): String {
    return String.format("#%06X", (0xFFFFFF and this))
}

fun String.toIntColor(): Int {
    return Color.parseColor(this)
}
