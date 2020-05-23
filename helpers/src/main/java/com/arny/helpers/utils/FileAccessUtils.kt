package com.arny.helpers.utils

import android.content.Context
import android.net.Uri

class FileAccessUtils {
    companion object {
        fun getFileName(context: Context, fileUri: Uri, columnName: String): String {
            var name = ""
            val returnCursor = context.contentResolver.query(fileUri, null, null, null, null)
            if (returnCursor != null) {
                val nameIndex = returnCursor.getColumnIndex(columnName)
                returnCursor.moveToFirst()
                name = returnCursor.getString(nameIndex)
                returnCursor.close()
            }
            return name
        }

        fun getFileData(context: Context, fileUri: Uri): String {
            return Utility.dumpCursor(context.contentResolver.query(fileUri, null, null, null, null))
        }
    }
}