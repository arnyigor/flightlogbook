package com.arny.domain.common

import android.graphics.drawable.Drawable
import com.arny.data.repositories.MainRepositoryImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommonUseCase @Inject constructor(private val repository: MainRepositoryImpl) {

    fun getString(res: Int): String {
        return repository.getString(res)
    }

    fun getColor(id: Int): Int {
        return repository.getColor(id)
    }

    fun getDrawable(id: Int): Drawable? {
        return repository.getDrawable(id)
    }
}