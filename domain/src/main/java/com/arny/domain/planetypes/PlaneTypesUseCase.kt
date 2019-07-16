package com.arny.domain.planetypes

import com.arny.data.repositories.MainRepositoryImpl
import com.arny.domain.models.PlaneType
import com.arny.domain.models.toPlaneType
import com.arny.domain.models.toPlaneTypeEntity
import com.arny.helpers.coroutins.CompositeJob
import com.arny.helpers.coroutins.addTo
import com.arny.helpers.coroutins.launchAsync
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaneTypesUseCase @Inject constructor(private val repository: MainRepositoryImpl) {
    private val compositeJob = CompositeJob()

    fun clearCompositJob() {
        compositeJob.clear()
    }

    fun loadPlaneTypes(onSuccess: (List<PlaneType>) -> Unit, onError: (String?) -> Unit? = {}) {
        launchAsync({
            repository.loadPlaneTypes().map { it.toPlaneType() }
        }, {
            onSuccess.invoke(it)
        }, {
            it.printStackTrace()
            onError.invoke(it.message)
        }).addTo(compositeJob)
    }

    fun addType(name: String, onSuccess: (Boolean) -> Unit, onError: (String?) -> Unit? = {}) {
        launchAsync({
            repository.addType(name)
        }, {
            onSuccess.invoke(it)
        }, {
            it.printStackTrace()
            onError.invoke(it.message)
        }).addTo(compositeJob)
    }

    fun removeType(item: PlaneType, onSuccess: (Boolean) -> Unit, onError: (String?) -> Unit? = {}) {
        launchAsync({
            repository.removeType(item.toPlaneTypeEntity())
        }, {
            onSuccess.invoke(it)
        }, {
            onError.invoke(it.message)
        }).addTo(compositeJob)
    }

    fun removeTypes(onSuccess: (Boolean) -> Unit, onError: (String?) -> Unit? = {}) {
        launchAsync({
            repository.removeTypes()
        }, {
            onSuccess.invoke(it)
        }, {
            onError.invoke(it.message)
        }).addTo(compositeJob)
    }

    fun updatePlaneTypeTitle(type: PlaneType, title: String?, onSuccess: (Boolean) -> Unit, onError: (String?) -> Unit? = {}) {
        launchAsync({
            repository.updatePlaneTypeTitle(type.toPlaneTypeEntity(), title)
        }, {
            onSuccess.invoke(it)
        }, {
            onError.invoke(it.message)
        }).addTo(compositeJob)
    }
}