package com.arny.flightlogbook.presentation.planetypes.list

import com.arny.domain.common.ResourcesInteractor
import com.arny.domain.models.PlaneType
import com.arny.domain.planetypes.PlaneTypesInteractor
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import com.arny.helpers.utils.fromSingle
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class PlaneTypesPresenter : BaseMvpPresenter<PlaneTypesView>() {
    @Inject
    lateinit var planeTypesInteractor: PlaneTypesInteractor
    @Inject
    lateinit var resourcesInteractor: ResourcesInteractor

    init {
        FlightApp.appComponent.inject(this)
    }

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
