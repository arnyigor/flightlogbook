package com.arny.flightlogbook.presentation.flighttypes

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.domain.common.CommonUseCase
import com.arny.domain.flighttypes.FlightTypesUseCase
import com.arny.domain.models.FlightType
import com.arny.flightlogbook.FlightApp
import com.arny.helpers.utils.addTo
import com.arny.helpers.utils.observeOnMain
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


@InjectViewState
class FlightTypesPresenter : MvpPresenter<FlightTypesView>() {
    private val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var flightTypesUseCase: FlightTypesUseCase
    @Inject
    lateinit var commonUseCase: CommonUseCase

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: FlightTypesView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    fun loadTypes() {
        viewState?.showEmptyView(false)
        flightTypesUseCase.loadDBFlightTypes()
                .observeOnMain()
                .subscribe({
                    if (it.isNotEmpty()) {
                        viewState?.showEmptyView(false)
                        viewState?.updateAdapter(it)
                    } else {
                        viewState?.showEmptyView(true)
                    }
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
        flightTypesUseCase.insertFlightType(title)
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
        flightTypesUseCase.removeFlightType(item.id)
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
        flightTypesUseCase.updateFlightType(item.id,newName)
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
