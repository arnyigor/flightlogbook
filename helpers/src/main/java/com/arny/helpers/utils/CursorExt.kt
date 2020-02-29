package com.arny.helpers.utils

import android.database.Cursor
import java.util.*

fun Cursor?.toList(onCursor: (c: Cursor) -> Unit) {
    if (this != null) {
        try {
            this.moveToPosition(-1);
            while (this.moveToNext()) {
                onCursor.invoke(this)
            }
        } catch (e: Exception) {
        } finally {
            this.close()
        }
    }
}

fun Cursor?.toItem(onCursor: (c: Cursor) -> Unit) {
    if (this != null) {
        try {
            if (this.moveToNext()) {
                onCursor.invoke(this)
            }
        } catch (e: Exception) {
        } finally {
            this.close()
        }
    }
}

fun Cursor.getLongValue(columnName: String?): Long {
    val index = this.getColumnIndex(columnName)
    return if (index != -1) {
        this.getLong(index)
    } else 0
}

fun Cursor.getDoubleValue(columnName: String?): Double {
    val cursorString = this.getStringValue(columnName)
    return if (cursorString != null && cursorString.trim { it <= ' ' }.isNotEmpty()) {
        cursorString.toDouble()
    } else {
        0.0
    }
}

fun Cursor.getBooleanValue(columnName: String?): Boolean {
    val string = this.getStringValue(columnName)
    return !string.isNullOrBlank() && string.toBoolean()
}

fun Cursor.getIntValue(columnName: String?): Int {
    val index = this.getColumnIndex(columnName)
    return if (index != -1) {
        this.getInt(index)
    } else 0
}

fun Cursor.getStringValue(columnName: String?): String? {
    val index = this.getColumnIndex(columnName)
    return if (index != -1) {
        this.getString(index)
    } else ""
}

fun Cursor?.dump(): String? {
    return Utility.dumpCursor(this)
}

fun Cursor.dumpColumns(): String {
    val cur = this
    val count = cur.columnCount
    val builder = StringBuilder()
    for (i in 0 until count) {
        val columnName = cur.getColumnName(i)
        var curValue: String? = null
        try {
            curValue = "[String]" + cur.getString(i)?.toString()
        } catch (e: Exception) {
        }
        if (curValue == null) {
            try {
                curValue = "[Long]" + cur.getLong(i).toString()
            } catch (e: Exception) {
            }
        }
        if (curValue == null) {
            try {
                curValue = "[Int]" + cur.getInt(i).toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        builder.append("$i:$columnName->$curValue\n")
    }
    return builder.toString()
}

fun Cursor.getColumns(): HashMap<String, Any> {
    val cur = this
    val count = cur.columnCount
    val mapColumns = hashMapOf<String, Any>()
    for (i in 0 until count) {
        val columnName = cur.getColumnName(i)
        var curValue: Any? = null
        try {
            curValue = cur.getString(i)
        } catch (e: Exception) {
        }
        if (curValue == null) {
            try {
                curValue = cur.getLong(i)
            } catch (e: Exception) {
            }
        }
        if (curValue == null) {
            try {
                curValue = cur.getInt(i)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (curValue != null) {
            mapColumns[columnName] = curValue
        } else {
            mapColumns[columnName] = "null"
        }
    }
    return mapColumns
}
