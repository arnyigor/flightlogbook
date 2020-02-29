package com.arny.domain.common

import android.graphics.drawable.Drawable
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
}