package com.arny.helpers.utils

import android.R
import android.graphics.Color
import kotlin.math.sqrt

private val COLORS_LIST = listOf(
    "#00FF00",
    "#FF0000",
    "#01FFFE",
    "#FFA6FE",
    "#FFDB66",
    "#006401",
    "#010067",
    "#95003A",
    "#007DB5",
    "#FF00F6",
    "#FFEEE8",
    "#774D00",
    "#90FB92",
    "#0076FF",
    "#D5FF00",
    "#FF937E",
    "#6A826C",
    "#FF029D",
    "#FE8900",
    "#7A4782",
    "#7E2DD2",
    "#85A900",
    "#FF0056",
    "#A42400",
    "#00AE7E",
    "#683D3B",
    "#BDC6FF",
    "#263400",
    "#BDD393",
    "#00B917",
    "#9E008E",
    "#001544",
    "#C28C9F",
    "#FF74A3",
    "#01D0FF",
    "#004754",
    "#E56FFE",
    "#788231",
    "#0E4CA1",
    "#91D0CB",
    "#BE9970",
    "#968AE8",
    "#BB8800",
    "#43002C",
    "#DEFF74",
    "#00FFC6",
    "#FFE502",
    "#620E00",
    "#008F9C",
    "#98FF52",
    "#7544B1",
    "#B500FF",
    "#00FF78",
    "#FF6E41",
    "#005F39",
    "#6B6882",
    "#5FAD4E",
    "#A75740",
    "#A5FFD2",
    "#FFB167",
    "#009BFF",
    "#E85EBE"
)

fun getColorsIntArray(): IntArray {
    return COLORS_LIST
        .map { it.toIntColor() }
        .toIntArray()
}

fun colorWillBeMasked(color: Int): Boolean {
    if (R.color.transparent == color) return true
    val rgb = intArrayOf(
        Color.red(color),
        Color.green(color),
        Color.blue(color)
    )
    val brightness = sqrt(
        rgb[0] * rgb[0] * .241 + rgb[1] * rgb[1] * .691 + rgb[2] * rgb[2] * .068
    ).toInt()
    println("COLOR: $color, BRIGHT: $brightness")
    if (brightness <= 40) {
        return true
    } else if (brightness >= 215) {
        return false
    }
    return false
}

fun Int.toHexColor(): String {
    return String.format("#%06X", (0xFFFFFF and this))
}

fun String.toIntColor(): Int {
    return Color.parseColor(this)
}
