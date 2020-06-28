package com.arny.domain.common

import android.graphics.drawable.Drawable
import java.io.BufferedReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourcesInteractor @Inject constructor(private val resourcesProvider: ResourcesProvider) {

    fun getString(res: Int): String {
        return resourcesProvider.getString(res)
    }

    fun getColor(id: Int): Int {
        return resourcesProvider.getColor(id)
    }

    fun getDrawable(id: Int): Drawable? {
        return resourcesProvider.getDrawable(id)
    }

    fun getAssetFileString(fileName: String): String {
        val open = resourcesProvider.provideContext().assets.open(fileName)
        val content = StringBuilder()
        BufferedReader(open.reader()).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                content.append(line)
                line = reader.readLine()
            }
        }
        return content.toString()
    }
}