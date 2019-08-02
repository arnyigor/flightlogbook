package com.arny.domain.planetypes

import com.arny.data.repositories.MainRepositoryImpl
import com.arny.domain.models.PlaneType
import com.arny.domain.models.toPlaneType
import com.arny.domain.models.toPlaneTypeEntity
import com.arny.helpers.utils.fromCallable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaneTypesUseCase @Inject constructor(private val repository: MainRepositoryImpl) {

    fun loadPlaneTypes(): Observable<List<PlaneType>> {
        return fromCallable { repository.loadPlaneTypes().map { it.toPlaneType() } }
    }

    fun addType(name: String ): Observable<Boolean> {
        return fromCallable { repository.addType(name) }
    }

    fun removeType(item: PlaneType): Observable<Boolean> {
        return fromCallable { repository.removeType(item.toPlaneTypeEntity()) }
    }

    fun removeTypes(): Observable<Boolean> {
        return fromCallable { repository.removeTypes() }
    }

    fun updatePlaneTypeTitle(id: Long?, title: String?): Observable<Boolean> {
        return fromCallable { repository.updatePlaneTypeTitle(id, title) }
    }
}