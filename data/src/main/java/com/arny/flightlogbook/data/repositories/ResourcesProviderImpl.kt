package com.arny.flightlogbook.data.repositories

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.ArrayRes
import androidx.core.content.ContextCompat
import com.arny.domain.common.IResourceProvider
import javax.inject.Inject

class ResourcesProviderImpl @Inject constructor(private val context: Context) : IResourceProvider {
    override fun getString(res: Int?): String {
        if (res == null) return ""
        return try {
            context.resources.getString(res)
        } catch (e: Resources.NotFoundException) {
            ""
        }
    }

    override fun getStringArray(@ArrayRes res: Int?): Array<String> {
        if (res == null) return emptyArray()
        return try {
            context.resources.getStringArray(res)
        } catch (e: Resources.NotFoundException) {
            emptyArray()
        }
    }

    override fun getColor(id: Int): Int {
        return ContextCompat.getColor(context, id)
    }

    override fun getDrawable(id: Int): Drawable? {
        return ContextCompat.getDrawable(context, id)
    }

    override fun provideContext(): Context = context

    override fun getContentResolver(): ContentResolver {
        return context.applicationContext.contentResolver
    }
}