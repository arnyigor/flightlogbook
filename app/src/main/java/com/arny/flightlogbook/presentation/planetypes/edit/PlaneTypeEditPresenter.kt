package com.arny.flightlogbook.presentation.planetypes.edit

import com.arny.domain.planetypes.AircraftType
import com.arny.domain.planetypes.PlaneTypesInteractor
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class PlaneTypeEditPresenter : BaseMvpPresenter<PlaneTypeEditView>() {

    var planeTypeId: Long? = null

    @Inject
    lateinit var planeTypesInteractor: PlaneTypesInteractor

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun onFirstViewAttach() {
        loadPlaneType()
    }

    private fun getMainTypeIndex(type: AircraftType?): Int {
        return when (type) {
            AircraftType.AIRPLANE -> 0
            AircraftType.HELICOPTER -> 1
            AircraftType.GLIDER -> 2
            AircraftType.AUTOGYRO -> 3
            AircraftType.AEROSTAT -> 4
            AircraftType.AIRSHIP -> 5
            else -> 0
        }

    }

    private fun loadPlaneType() {
        planeTypesInteractor.loadPlaneType(planeTypeId)
                .subscribeFromPresenter({
                    val planeType = it.value
                    if (planeType != null) {
                        viewState.setPlaneTypeName(planeType.typeName)
                        viewState.setMainPlaneType(getMainTypeIndex(planeType.mainType))
                        viewState.setRegNo(planeType.regNo)
                    }
                }, {
                    viewState.showError(it.message)
                })
    }
}
