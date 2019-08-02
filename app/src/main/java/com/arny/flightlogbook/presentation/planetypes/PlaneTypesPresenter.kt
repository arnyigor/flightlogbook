package com.arny.flightlogbook.presentation.planetypes

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.domain.common.CommonUseCase
import com.arny.domain.models.PlaneType
import com.arny.domain.planetypes.PlaneTypesUseCase
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.helpers.utils.addTo
import com.arny.helpers.utils.observeOnMain
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
    }

    fun loadTypes() {
        viewState?.setEmptyViewVisible(false)
        planeTypesUseCase.loadPlaneTypes()
                .observeOnMain()
                .subscribe({
                    if (it.isNotEmpty()) {
                        viewState?.setEmptyViewVisible(false)
                        viewState?.updateAdapter(it)
                    } else {
                        viewState?.setEmptyViewVisible(true)
                    }
                }, {
                    viewState?.setEmptyViewVisible(true)
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun addType(name: String) {
        planeTypesUseCase.addType(name)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        loadTypes()
                    } else {
                        viewState?.toastError(commonUseCase.getString(R.string.str_type_add_fail))
                    }
                }, {
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun removeType(item: PlaneType) {
        planeTypesUseCase.removeType(item)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        loadTypes()
                    } else {
                        viewState?.toastError(commonUseCase.getString(R.string.str_type_remove_fail))
                    }
                }, {
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun updatePlaneTypeTitle(type: PlaneType, title: String?, position: Int) {
        planeTypesUseCase.updatePlaneTypeTitle(type.typeId, title)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        type.typeName = title
                        viewState?.notifyItemChanged(position)
                    } else {
                        viewState?.toastError(commonUseCase.getString(R.string.str_type_change_fail))
                    }
                }, {
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }
}