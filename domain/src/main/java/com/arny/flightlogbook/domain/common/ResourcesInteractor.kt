package com.arny.flightlogbook.domain.common

import android.content.Context
import android.graphics.drawable.Drawable
import java.io.BufferedReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourcesInteractor @Inject constructor(
    private val resourcesProvider: IResourceProvider,
    private val context: Context
) {

    fun getString(res: Int?): String = resourcesProvider.getString(res)

    fun getColor(id: Int): Int = resourcesProvider.getColor(id)

    fun getDrawable(id: Int): Drawable? = resourcesProvider.getDrawable(id)

    fun getAssetFileString(fileName: String): String {
        val content = StringBuilder()
        BufferedReader(context.assets.open(fileName).reader()).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                content.append(line)
                line = reader.readLine()
            }
        }
        return content.toString()
    }
}