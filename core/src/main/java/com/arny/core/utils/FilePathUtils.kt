package com.arny.core.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FilePathUtils {
    companion object {
        private var selection: String? = null
        private var selectionArgs: Array<String>? = null
        private const val BUFFER_SIZE = 1024
        private const val MAX_BUFFER_SIZE = 1024 * 1024
        private const val DEFAULT_SCHEME = "file"
        private const val GOOGLE_DRIVE_CONTENT = "com.google.android.apps.docs.storage"
        private const val WHATS_APP_CONTENT = "com.whatsapp.provider.media"
        private const val GOOGLE_PHOTOS_CONTENT = "com.google.android.apps.photos.content"
        private const val MEDIA_DOCUMENT_CONTENT = "com.android.providers.media.documents"
        private const val DOWNLOAD_DOCUMENT_CONTENT = "com.android.providers.downloads.documents"
        private const val EXTERNAL_STORAGE_CONTENT = "com.android.externalstorage.documents"

        fun getFileName(uri: Uri, context: Context): String? {
            if (uri.scheme == "content") {
                val fileNameFromContentUri = tryGetFileNameFromContentUri(uri, context)
                if (!fileNameFromContentUri.isNullOrBlank()) return fileNameFromContentUri
            }
            var name = uri.lastPathSegment
            val fileSeparator = File.separator
            if (!name.isNullOrBlank()) {
                if (name.contains(fileSeparator)) {
                    val nameStartIndex = name.lastIndexOf(fileSeparator)
                    name = name.substring(nameStartIndex + 1)
                }
                name = name.replace(":".toRegex(), "")
                if (DEFAULT_SCHEME != uri.scheme) {
                    val ext = "." + MimeTypeMap.getSingleton()
                        .getExtensionFromMimeType(context.contentResolver.getType(uri))
                    if (!name.contains(ext)) name += ext
                }
            }
            return name
        }

        private fun tryGetFileNameFromContentUri(uri: Uri, context: Context): String? {
            try {
                context.contentResolver.query(uri, null, null, null, null).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        return cursor.getString(columnIndex)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        private fun tryGetFileNameFromContentUri(context: Context,uri: Uri): String? {
            try {
                context.contentResolver.query(uri, null, null, null, null).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        return cursor.getString(columnIndex)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun getPath(uri: Uri?, context: Context): String? {
            if (uri == null) return null
            return handlePaths(uri, context)
        }

        @SuppressLint("NewApi")
        private fun handleExternalStorage(uri: Uri): String? {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).toTypedArray()
            val fullPath = getPathFromExtSD(split)
            return fullPath.ifBlank { null }
        }

        @SuppressLint("NewApi")
        private fun handleDownloads(uri: Uri, context: Context): String? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                handleDownloads23ApiAndAbove(uri, context)
            } else {
                handleDownloadsBelow23Api(uri, context)
            }
        }

        @SuppressLint("NewApi")
        private fun handleDownloadsBelow23Api(uri: Uri, context: Context): String? {
            val id = DocumentsContract.getDocumentId(uri)
            if (id.startsWith("raw:")) return id.replaceFirst("raw:".toRegex(), "")
            var contentUri: Uri? = null
            try {
                contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong()
                )
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            if (contentUri != null) return getDataColumn(contentUri, context)
            return null
        }

        @SuppressLint("NewApi")
        private fun handleDownloads23ApiAndAbove(uri: Uri, context: Context): String? {
            var path: String? = null
            val cursor = context.contentResolver
                .query(
                    uri,
                    arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )
            cursor?.use {
                it.moveToFirst()
                path =
                    "${Environment.getExternalStorageDirectory()}/Download/${cursor.getString(0)}"
            }
            return if (!path.isNullOrBlank()) {
                path
            } else {
                handleDocumentId(uri, context)
            }
        }

        private fun handleDocumentId(
            uri: Uri,
            context: Context
        ): String? {
            val id: String = DocumentsContract.getDocumentId(uri)
            if (id.isNotEmpty()) {
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:".toRegex(), "")
                }
                val contentUriPrefixesToTry = arrayOf(
                    "content://downloads/public_downloads",
                    "content://downloads/my_downloads"
                )
                for (contentUriPrefix in contentUriPrefixesToTry) {
                    return try {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse(contentUriPrefix),
                            id.toLong()
                        )
                        getDataColumn(contentUri, context)
                    } catch (e: NumberFormatException) {
                        //In Android 8 and Android P the id is not a number
                        uri.path.orEmpty().replaceFirst("^/document/raw:".toRegex(), "")
                            .replaceFirst("^raw:".toRegex(), "")
                    }
                }
            }
            return null
        }

        @SuppressLint("NewApi")
        private fun handleMedia(uri: Uri, context: Context): String? {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).toTypedArray()
            val contentUri: Uri? = when (split[0]) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> null
            }
            selection = "_id=?"
            selectionArgs = arrayOf(split[1])
            return if (contentUri != null) getDataColumn(contentUri, context) else null
        }

        private fun handleContentScheme(uri: Uri, context: Context): String? {
            if (isGooglePhotosUri(uri)) return uri.lastPathSegment
            if (isGoogleDriveUri(uri)) return getDriveFilePath(uri, context)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                copyFileToInternalStorage(uri, "userfiles", context)
            } else {
//                getDataColumn(uri, context)
                getFilePathFromUri(context, uri)
            }
        }

        private fun handlePaths(uri: Uri, context: Context): String? = when {
            // ExternalStorageProvider
            isExternalStorageDocument(uri) -> handleExternalStorage(uri)
            // DownloadsProvider
            isDownloadsDocument(uri) -> handleDownloads(uri, context)
            // MediaProvider
            isMediaDocument(uri) -> handleMedia(uri, context) ?: handleDownloads(uri, context)
            //GoogleDriveProvider
            isGoogleDriveUri(uri) -> getDriveFilePath(uri, context)
            //WhatsAppProvider
            isWhatsAppFile(uri) -> getFilePathForWhatsApp(uri, context)
            //ContentScheme
            "content".equals(uri.scheme, ignoreCase = true) -> handleContentScheme(uri, context)
            //FileScheme
            "file".equals(uri.scheme, ignoreCase = true) -> uri.path
            else -> null
        }

        private fun getFilePathFromUri(context: Context, _uri: Uri?): String? {
            var filePath: String? = ""
            if (_uri != null && "content" == _uri.scheme) {
                //Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
                //context.revokeUriPermission(_uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                var cursor: Cursor? = null
                try {
                    cursor = context.contentResolver.query(
                        _uri, arrayOf(
                            MediaStore.Files.FileColumns.DATA,
                        ), null, null, null
                    )
                    cursor!!.moveToFirst()
                    filePath = cursor.getString(0)
                } catch (e: SecurityException) {
                    //if file open with third party application
                    if (_uri.toString().contains("/storage/emulated/0")) {
                        filePath = "/storage/emulated/0" + _uri.toString()
                            .split("/storage/emulated/0".toRegex()).toTypedArray()[1]
                    }
                } finally {
                    cursor?.close()
                }
            } else {
                filePath = _uri!!.path
            }
            return filePath
        }

        private fun fileExists(filePath: String): Boolean = File(filePath).exists()

        private fun getPathFromExtSD(pathData: Array<String>): String {
            val type = pathData[0]
            val relativePath = "/" + pathData[1]
            var fullPath: String
            // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
            // something like "71F8-2C0A", some kind of unique id per storage
            // don't know any API that can get the root path of that storage based on its id.
            //
            // so no "primary" type, but let the check here for other devices
            if ("primary".equals(type, ignoreCase = true)) {
                fullPath = Environment.getExternalStorageDirectory().toString() + relativePath
                if (fileExists(fullPath)) {
                    return fullPath
                }
            }
            // Environment.isExternalStorageRemovable() is `true` for external and internal storage
            // so we cannot relay on it.
            //
            // instead, for each possible path, check if file exists
            // we'll start with secondary storage as this could be our (physically) removable sd card
            fullPath = System.getenv("SECONDARY_STORAGE").orEmpty() + relativePath
            if (fileExists(fullPath)) {
                return fullPath
            }
            fullPath = System.getenv("EXTERNAL_STORAGE").orEmpty() + relativePath
            return if (fileExists(fullPath)) {
                fullPath
            } else fullPath
        }

        private fun getDriveFilePath(uri: Uri, context: Context): String? {
            context.contentResolver.query(
                uri, null, null, null, null
            )?.use { cursor ->
                /*
                 * Get the column indexes of the data in the Cursor,
                 *     * move to the first row in the Cursor, get the data,
                 *     * and display it.
                 * */
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                val name = cursor.getString(nameIndex)
                val file = File(context.cacheDir, name)
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)!!
                    val outputStream = FileOutputStream(file)
                    val bytesAvailable = inputStream.available()
                    val bufferSize = bytesAvailable.coerceAtMost(MAX_BUFFER_SIZE)
                    val buffers = ByteArray(bufferSize)
                    var read: Int
                    while (inputStream.read(buffers).also { read = it } != -1) {
                        outputStream.write(buffers, 0, read)
                    }
                    inputStream.close()
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    cursor.close()
                }
                return file.path
            }
            return null
        }

        /***
         * Used for Android Q+
         * @param uri
         * @param newDirName if you want to create a directory, you can set this variable
         * @return
         */
        private fun copyFileToInternalStorage(
            uri: Uri,
            newDirName: String,
            context: Context
        ): String? {
            context.contentResolver.query(
                uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null
            )?.use { cursor ->
                /*
                 * Get the column indexes of the data in the Cursor,
                 *     * move to the first row in the Cursor, get the data,
                 *     * and display it.
                 * */
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                val name = cursor.getString(nameIndex)
                val output: File = if (newDirName != "") {
                    val dir = File(context.filesDir.toString() + "/" + newDirName)
                    if (!dir.exists()) {
                        dir.mkdir()
                    }
                    File(context.filesDir.toString() + "/" + newDirName + "/" + name)
                } else {
                    File(context.filesDir.toString() + "/" + name)
                }
                try {
                    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
                    val outputStream = FileOutputStream(output)
                    var read: Int
                    val buffers = ByteArray(BUFFER_SIZE)
                    while (inputStream.read(buffers).also { read = it } != -1) {
                        outputStream.write(buffers, 0, read)
                    }
                    inputStream.close()
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    cursor.close()
                }
                return output.path
            }
            return null
        }

        private fun getFilePathForWhatsApp(uri: Uri, context: Context): String? {
            return copyFileToInternalStorage(uri, "whatsapp", context)
        }

        private fun getDataColumn(uri: Uri, context: Context): String? {
            val cursor: Cursor?
            val column = "_data"
            val projection = arrayOf(column)
            cursor = context.contentResolver.query(
                uri, projection,
                selection, selectionArgs, null
            )
            var path: String? = null
            cursor?.use {
                path = cursor.getString(cursor.getColumnIndexOrThrow(column))
            }
            return path
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean =
            EXTERNAL_STORAGE_CONTENT == uri.authority

        private fun isDownloadsDocument(uri: Uri): Boolean =
            DOWNLOAD_DOCUMENT_CONTENT == uri.authority

        private fun isMediaDocument(uri: Uri): Boolean = MEDIA_DOCUMENT_CONTENT == uri.authority

        private fun isGooglePhotosUri(uri: Uri): Boolean = GOOGLE_PHOTOS_CONTENT == uri.authority

        private fun isWhatsAppFile(uri: Uri): Boolean = WHATS_APP_CONTENT == uri.authority

        private fun isGoogleDriveUri(uri: Uri): Boolean =
            GOOGLE_DRIVE_CONTENT == uri.authority || "com.google.android.apps.docs.storage.legacy" == uri.authority
    }
}
