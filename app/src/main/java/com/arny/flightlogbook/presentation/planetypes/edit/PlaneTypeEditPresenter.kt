package com.arny.flightlogbook.presentation.planetypes.edit

import com.arny.domain.planetypes.AircraftType
import com.arny.domain.planetypes.PlaneTypesInteractor
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
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

    private fun getMainTypeIndex(type: AircraftType?) = when (type) {
        AircraftType.AIRPLANE -> 0
        AircraftType.HELICOPTER -> 1
        AircraftType.GLIDER -> 2
        AircraftType.AUTOGYRO -> 3
        AircraftType.AEROSTAT -> 4
        AircraftType.AIRSHIP -> 5
        else -> 0
    }

    private fun getType(index: Int) = when (index) {
        0 -> AircraftType.AIRPLANE
        1 -> AircraftType.HELICOPTER
        2 -> AircraftType.GLIDER
        3 -> AircraftType.AUTOGYRO
        4 -> AircraftType.AEROSTAT
        5 -> AircraftType.AIRSHIP
        else -> AircraftType.AIRPLANE
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

    fun onSavePlaneType(title: String, regNo: String, position: Int) {
        if (title.isBlank()) {
            viewState.showTitleError(R.string.error_empty_text_field)
            return
        }
        if (regNo.isBlank()) {
            viewState.showRegNoError(R.string.error_empty_text_field)
            return
        }
        planeTypesInteractor.addType(planeTypeId, title, regNo, getType(position))
                .subscribeFromPresenter({
                    if (it != 0L) {
                        viewState.setResultOk(it)
                    } else {
                        viewState.showError(R.string.error_plane_type_not_saved)
                    }
                }, {
                    viewState.showError(it.message)
                })
    }
}
