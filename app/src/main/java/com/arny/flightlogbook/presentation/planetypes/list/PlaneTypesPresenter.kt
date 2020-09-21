package com.arny.flightlogbook.presentation.planetypes.list

import com.arny.domain.common.ResourcesInteractor
import com.arny.domain.models.PlaneType
import com.arny.domain.planetypes.PlaneTypesInteractor
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.helpers.utils.addTo
import com.arny.helpers.utils.observeOnMain
import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class PlaneTypesPresenter : MvpPresenter<PlaneTypesView>() {
    private val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var planeTypesInteractor: PlaneTypesInteractor
    @Inject
    lateinit var resourcesInteractor: ResourcesInteractor

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: PlaneTypesView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    fun loadTypes() {
        viewState?.setEmptyViewVisible(false)
        planeTypesInteractor.loadPlaneTypes()
                .observeOnMain()
                .subscribe({
                    viewState?.updateAdapter(it)
                    viewState?.setEmptyViewVisible(it.isEmpty())
                }, {
                    viewState?.setEmptyViewVisible(true)
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun removeType(item: PlaneType) {
        planeTypesInteractor.removeType(item)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        loadTypes()
                    } else {
                        viewState?.toastError(resourcesInteractor.getString(R.string.str_type_remove_fail))
                    }
                }, {
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun updatePlaneTypeTitle(type: PlaneType, title: String?, position: Int) {
        planeTypesInteractor.updatePlaneTypeTitle(type.typeId, title)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        type.typeName = title
                        viewState?.notifyItemChanged(position)
                    } else {
                        viewState?.toastError(resourcesInteractor.getString(R.string.str_type_change_fail))
                    }
                }, {
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }
}