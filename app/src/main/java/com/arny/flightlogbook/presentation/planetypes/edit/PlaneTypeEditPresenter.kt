package com.arny.flightlogbook.presentation.planetypes.edit

import com.arny.core.utils.fromNullable
import com.arny.core.utils.fromSingle
import com.arny.flightlogbook.R
import com.arny.flightlogbook.domain.planetypes.AircraftType
import com.arny.flightlogbook.domain.planetypes.PlaneTypesInteractor
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class PlaneTypeEditPresenter @Inject constructor(
    private val planeTypesInteractor: PlaneTypesInteractor
) : BaseMvpPresenter<PlaneTypeEditView>() {
    var planeTypeId: Long? = null
        set(value) {
            field = if (value != -1L) value else null
        }

    override fun onFirstViewAttach() {
        loadPlaneType()
    }

    private fun loadPlaneType() {
        fromNullable { planeTypesInteractor.loadPlaneType(planeTypeId) }
            .subscribeFromPresenter({
                val planeType = it.value
                if (planeType != null) {
                    viewState.setPlaneTypeName(planeType.typeName)
                    viewState.setMainPlaneType(planeType.mainType?.mainType ?: 0)
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
        fromSingle {
            planeTypesInteractor.addType(
                planeTypeId,
                title,
                regNo,
                AircraftType.getType(position)
            )
        }
            .subscribeFromPresenter({ save ->
                if (save) {
                    viewState.setResultOk()
                } else {
                    viewState.showError(R.string.error_plane_type_not_saved)
                }
            }, {
                viewState.showError(it.message)
            })
    }
}
