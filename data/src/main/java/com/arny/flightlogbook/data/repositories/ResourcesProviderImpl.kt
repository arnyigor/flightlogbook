package com.arny.flightlogbook.data.repositories

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.arny.domain.common.ResourcesProvider
import javax.inject.Inject

class ResourcesProviderImpl @Inject constructor(private val context: Context) : ResourcesProvider {
    override fun getString(res: Int?): String {
        if (res == null) return ""
        return try {
            context.resources.getString(res)
        } catch (e: Resources.NotFoundException) {
            ""
        }
    }

    override fun getColor(id: Int): Int {
        return ContextCompat.getColor(context, id)
    }

    override fun getDrawable(id: Int): Drawable? {
        return ContextCompat.getDrawable(context, id)
    }

    override fun provideContext(): Context {
        return context
    }
}