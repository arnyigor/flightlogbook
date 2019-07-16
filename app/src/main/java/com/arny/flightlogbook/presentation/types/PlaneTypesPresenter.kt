package com.arny.flightlogbook.presentation.types

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.domain.common.CommonUseCase
import com.arny.domain.models.PlaneType
import com.arny.domain.planetypes.PlaneTypesUseCase
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@InjectViewState
class PlaneTypesPresenter : MvpPresenter<PlaneTypesView>() {
    private val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var planeTypesUseCase: PlaneTypesUseCase
    @Inject
    lateinit var commonUseCase: CommonUseCase

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: PlaneTypesView?) {
        super.detachView(view)
        compositeDisposable.clear()
        planeTypesUseCase.clearCompositJob()
    }

    fun loadTypes() {
        planeTypesUseCase.loadPlaneTypes({
            if (it.isNotEmpty()) {
                viewState?.setAdapterVisible(true)
                viewState?.setEmptyViewVisible(false)
                viewState?.setBtnRemoveAllVisible(true)
                viewState?.updateAdapter(it)
            } else {
                viewState?.clearAdapter()
                viewState?.setAdapterVisible(false)
                viewState?.setEmptyViewVisible(true)
                viewState?.setBtnRemoveAllVisible(false)
            }
        }, {
            viewState?.setAdapterVisible(false)
            viewState?.setEmptyViewVisible(true)
            viewState?.setBtnRemoveAllVisible(false)
            viewState?.toastError(it)
        })
    }

    fun addType(name: String) {
        planeTypesUseCase.addType(name, {
            if (it) {
                loadTypes()
            } else {
                viewState?.toastError(commonUseCase.getString(R.string.str_type_add_fail))
            }
        }, {
            viewState?.toastError(it)
        })
    }

    fun removeType(item: PlaneType) {
        planeTypesUseCase.removeType(item, {
            if (it) {
                loadTypes()
            } else {
                viewState?.toastError(commonUseCase.getString(R.string.str_type_remove_fail))
            }
        }, {
            viewState?.toastError(it)
        })
    }

    fun removeAllPlaneTypes() {
        planeTypesUseCase.removeTypes({
            if (it) {
                viewState?.clearAdapter()
                viewState?.setAdapterVisible(false)
                viewState?.setEmptyViewVisible(true)
                viewState?.setBtnRemoveAllVisible(false)
            } else {
                viewState?.toastError(commonUseCase.getString(R.string.str_types_removes_fail))
            }
        }, {
            viewState?.toastError(it)
        })
    }

    fun updatePlaneTypeTitle(type: PlaneType, title: String?, position: Int) {
        planeTypesUseCase.updatePlaneTypeTitle(type, title, {
            if (it) {
                type.typeName = title
                viewState?.notifyItemChanged(position)
            } else {
                viewState?.toastError(commonUseCase.getString(R.string.str_type_change_fail))
            }
        }, {
            viewState?.toastError(it)
        })
    }

}