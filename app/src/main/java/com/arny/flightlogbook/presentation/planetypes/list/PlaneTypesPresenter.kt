package com.arny.flightlogbook.presentation.planetypes.list

import com.arny.core.utils.fromSingle
import com.arny.flightlogbook.R
import com.arny.flightlogbook.domain.common.ResourcesInteractor
import com.arny.flightlogbook.domain.models.PlaneType
import com.arny.flightlogbook.domain.planetypes.PlaneTypesInteractor
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class PlaneTypesPresenter @Inject constructor(
    private val planeTypesInteractor: PlaneTypesInteractor,
    private val resourcesInteractor: ResourcesInteractor
) : BaseMvpPresenter<PlaneTypesView>() {
    fun loadTypes() {
        viewState?.setEmptyViewVisible(false)
        fromSingle { planeTypesInteractor.loadPlaneTypes() }
            .subscribeFromPresenter({
                viewState?.updateAdapter(it)
                viewState?.setEmptyViewVisible(it.isEmpty())
            }, {
                viewState?.setEmptyViewVisible(true)
                viewState?.toastError(it.message)
            })
    }

    fun removeType(item: PlaneType) {
        fromSingle { planeTypesInteractor.removeType(item) }
            .subscribeFromPresenter({
                if (it) {
                    loadTypes()
                } else {
                    viewState?.toastError(resourcesInteractor.getString(R.string.str_type_remove_fail))
                }
            }, {
                viewState?.toastError(it.message)
            })
    }
}
