package com.arny.flightlogbook.presenter.types

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.source.MainRepositoryImpl
import javax.inject.Inject

@InjectViewState
class TypeListPresenterImpl : MvpPresenter<TypeListView>(), TypeListPresenter {
    @Inject
    lateinit var repository: MainRepositoryImpl

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun addType(name: String) {
        repository.addType(name, {
            if (it) {
                viewState?.toastSuccess(repository.getString(R.string.str_type_add_succesfull))
            } else {
                viewState?.toastError(repository.getString(R.string.str_type_add_fail))
            }
        }, {
            viewState?.toastError(it.message)
        })
    }

    override fun confirmEditType(item: AircraftType) {
        repository.loadType(item.typeId, {
            viewState?.showEditDialog(item)
        }, {
            viewState?.toastError(it.message)
        })
    }

    override fun confirmDeleteType(item: AircraftType) {
        repository.loadType(item.typeId, {
            viewState?.showRemoveDialog(item)
        }, {
            viewState?.toastError(it.message)
        })
    }

    override fun removeType(item: AircraftType) {
        repository.removeType(item, {
            if (it) {
                viewState?.toastSuccess(repository.getString(R.string.str_type_remove_succesfull))
            } else {
                viewState?.toastError(repository.getString(R.string.str_type_remove_fail))
            }
        }, {
            viewState?.toastError(it.message)
        })
    }

    override fun updateType(type: AircraftType) {
        repository.updateType(type, {
            if (it) {
                viewState?.toastSuccess(repository.getString(R.string.str_type_change_succesfull))
            } else {
                viewState?.toastError(repository.getString(R.string.str_type_change_fail))
            }
        }, {
            viewState?.toastError(it.message)
        })
    }
}