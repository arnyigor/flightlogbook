package com.arny.flightlogbook.presentation.planetypes.edit

import com.arny.core.utils.toOptionalNull
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.domain.planetypes.PlaneTypesInteractor
import com.arny.flightlogbook.presentation.mvp.BaseMvpPresenter
import io.reactivex.Single
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
        planeTypeId?.let { id ->
            loadPlaneType(id)
            viewState.updateTitle(R.string.edit_plane_type)
        } ?: kotlin.run {
            viewState.updateTitle(R.string.add_plane_type)
        }
    }

    private fun loadPlaneType(id: Long) {
        Single.fromCallable { planeTypesInteractor.loadPlaneType(id).toOptionalNull() }
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
        Single.fromCallable {
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
