package com.arny.domain.planetypes

import com.arny.domain.models.PlaneType
import com.arny.helpers.utils.fromCallable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaneTypesInteractor @Inject constructor(private val planeTypesRepository: PlaneTypesRepository) {
    fun loadPlaneTypes(): Observable<List<PlaneType>> {
        return fromCallable { planeTypesRepository.loadPlaneTypes() }
    }

    fun addType(name: String): Observable<Boolean> {
        return fromCallable { planeTypesRepository.addType(name) }
    }

    fun removeType(item: PlaneType): Observable<Boolean> {
        return fromCallable { planeTypesRepository.removeType(item) }
    }

    fun removeTypes(): Observable<Boolean> {
        return fromCallable { planeTypesRepository.removeTypes() }
    }

    fun updatePlaneTypeTitle(id: Long?, title: String?): Observable<Boolean> {
        return fromCallable { planeTypesRepository.updatePlaneTypeTitle(id, title) }
    }
}