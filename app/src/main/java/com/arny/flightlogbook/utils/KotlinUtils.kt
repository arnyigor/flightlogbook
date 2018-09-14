package com.arny.flightlogbook.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import java.util.*

fun <T> find(list: List<T>, c: T, comp: Comparator<T>): T? {
    return list.firstOrNull { comp.compare(c, it) == 0 }
}

@JvmOverloads
fun transliterate(message: String, toUpper: Boolean = false): String {
    val abcCyr = charArrayOf(' ', 'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
    val abcLat = arrayOf(" ", "a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sch", "", "i", "", "e", "ju", "ja", "A", "B", "V", "G", "D", "E", "E", "Zh", "Z", "I", "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "H", "Ts", "Ch", "Sh", "Sch", "", "I", "", "E", "Ju", "Ja", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
    val builder = StringBuilder()
    for (i in 0 until message.length) {
        for (x in abcCyr.indices) {
            if (message[i] == abcCyr[x]) {
                builder.append(abcLat[x])
            }
        }
    }
    var res = builder.toString()
    if (toUpper) {
        res = res.toUpperCase()
    }
    return res.trim()
}

fun <T> findPosition(list: List<T>, item: T): Int {
    return list.indexOf(item)
}

fun <T> findPosition(list: Array<T>, item: T): Int {
    return list.indexOf(item)
}


fun <T> getExcludeList(list: ArrayList<T>, items: List<T>, comparator: Comparator<T>): ArrayList<T> {
    val res = ArrayList<T>()
    for (t in list) {
        val pos = Collections.binarySearch(items, t, comparator)
        if (pos < 0) {
            res.add(t)
        }
    }
    return res
}

fun getSQLType(fieldType: String): String {
    val res = when {
        fieldType.equals("int", true) -> "INTEGER"
        fieldType.equals("integer", true) -> "INTEGER"
        fieldType.equals("float", true) -> "REAL"
        fieldType.equals("double", true) -> "REAL"
        fieldType.equals("string", true) -> "TEXT"
        fieldType.equals("char", true) -> "TEXT"
        fieldType.equals("byte", true) -> "TEXT"
        else -> "TEXT"
    }
    return res
}

fun <T> getIntentExtra(intent: Intent?, extraName: String): T? {
    return intent?.extras?.get(extraName) as? T
}

fun <T> getBundleExtra(extras: Bundle?, extraName: String): T? {
    return extras?.get(extraName) as? T
}

/**
 * Универсальная функция окончаний
 * @param [count] число
 * @param [zero_other] слово с окончанием значения  [count] либо ноль,либо все остальные варианты включая от 11 до 19 (слов)
 * @param [one] слово с окончанием значения  [count]=1 (слово)
 * @param [two_four] слово с окончанием значения  [count]=2,3,4 (слова)
 */
fun getTermination(count: Int, zero_other: String, one: String, two_four: String): String {
    if (count % 100 in 11..19) {
        return count.toString() + " " + zero_other
    }
    return when (count % 10) {
        1 -> count.toString() + " " + one
        2, 3, 4 -> count.toString() + " " + two_four
        else -> count.toString() + " " + zero_other
    }
}

fun View?.setVisible(visible: Boolean) {
    this?.visibility = if (visible) View.VISIBLE else View.GONE
}

/**
 * safety String? to Double
 */
fun String?.setDouble(): Double {
    val source = this ?: ""
    if (source.isBlank() || source==".") {
        return 0.0
    }
    return source.toDouble()
}

fun TextView?.setString(text: String?): TextView? {
    this?.clearFocus()
    this?.tag = ""
    this?.text = text
    this?.tag = null
    return this
}

fun animateVisible(v: View, visible: Boolean, duration: Int) {
    val alpha = if (visible) 1.0f else 0.0f
    v.clearAnimation()
    v.animate()
            .alpha(alpha)
            .setDuration(duration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    v.setVisible(visible)
                }
            })
}
