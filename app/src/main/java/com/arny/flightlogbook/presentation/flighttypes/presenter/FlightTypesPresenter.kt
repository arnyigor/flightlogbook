package com.arny.flightlogbook.presentation.flighttypes.presenter

import com.arny.domain.common.ResourcesInteractor
import com.arny.domain.flighttypes.FlightTypesInteractor
import com.arny.domain.models.FlightType
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.presentation.flighttypes.view.FlightTypesView
import com.arny.helpers.utils.addTo
import com.arny.helpers.utils.observeOnMain
import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject


@InjectViewState
class FlightTypesPresenter : MvpPresenter<FlightTypesView>() {
    private val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var flightTypesInteractor: FlightTypesInteractor
    @Inject
    lateinit var resourcesInteractor: ResourcesInteractor

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: FlightTypesView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    fun loadTypes() {
        viewState?.showEmptyView(false)
        flightTypesInteractor.loadDBFlightTypes()
                .observeOnMain()
                .subscribe({
                    viewState?.updateAdapter(it)
                    viewState?.showEmptyView(it.isEmpty())
                }, {
                    viewState?.showEmptyView(true)
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun addType(title: String?) {
        if (title.isNullOrBlank()) {
            viewState?.toastError("Введите название типа полета")
            return
        }
        flightTypesInteractor.insertFlightType(title)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        loadTypes()
                    }else{
                        viewState?.toastError("Тип полета не добавлен")
                    }
                },{
                    it.printStackTrace()
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)

    }

    fun removeFlightType(item: FlightType) {
        flightTypesInteractor.removeFlightType(item.id)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        loadTypes()
                    }else{
                        viewState?.toastError("Тип не удален")
                    }
                },{
                    it.printStackTrace()
                    viewState?.toastError("Ошибка удаления типа")
                })
                .addTo(compositeDisposable)
    }

    fun editFlightTypeTitle(item: FlightType, newName: String) {
        flightTypesInteractor.updateFlightType(item.id,newName)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        loadTypes()
                    }else{
                        viewState?.toastError("Тип не обновлен")
                    }
                },{
                    it.printStackTrace()
                    viewState?.toastError("Ошибка обновления типа")
                })
                .addTo(compositeDisposable)
    }

}
