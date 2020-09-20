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

    private fun getAircraftType(index: Int?): AircraftType {
        return when (index) {
            0 -> AircraftType.AIRPLANE
            1 -> AircraftType.HELICOPTER
            2 -> AircraftType.GLIDER
            3 -> AircraftType.AUTOGYRO
            4 -> AircraftType.AEROSTAT
            5 -> AircraftType.AIRSHIP
            else -> AircraftType.AIRPLANE
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
                    viewState.toastError(it.message)
                })
    }

    fun onBtnSaveClicked(planeTitle: String, regNo: String, typeIndex: Int) {
        planeTypesInteractor.addType(planeTitle, regNo, getAircraftType(typeIndex))
                .subscribeFromPresenter({
                    if (it) {
                        viewState.onResultSuccess()
                    } else {
                        viewState.toastError(R.string.str_type_add_fail)
                    }
                }, {
                    viewState.toastError(it.message)
                })
    }
}
