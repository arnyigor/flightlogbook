package com.arny.flightlogbook.presentation.flighttypes.list

import com.arny.flightlogbook.data.models.FlightType
import com.arny.flightlogbook.domain.flighttypes.FlightTypesInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class FlightTypesPresenter @Inject constructor(
    private val flightTypesInteractor: FlightTypesInteractor
) : MvpPresenter<FlightTypesView>() {
    private val compositeDisposable = CompositeDisposable()

    override fun detachView(view: FlightTypesView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    fun loadTypes() {
        viewState?.showEmptyView(false)
        flightTypesInteractor.loadDBFlightTypes()
            .observeOn(AndroidSchedulers.mainThread())
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
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it) {
                    loadTypes()
                } else {
                    viewState?.toastError("Тип полета не добавлен")
                }
            }, {
                it.printStackTrace()
                viewState?.toastError(it.message)
            })
            .addTo(compositeDisposable)

    }

    fun removeFlightType(item: FlightType) {
        flightTypesInteractor.removeFlightType(item.id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it) {
                    loadTypes()
                } else {
                    viewState?.toastError("Тип не удален")
                }
            }, {
                it.printStackTrace()
                viewState?.toastError("Ошибка удаления типа")
            })
            .addTo(compositeDisposable)
    }

    fun editFlightTypeTitle(item: FlightType, newName: String) {
        flightTypesInteractor.updateFlightType(item.id, newName)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it) {
                    loadTypes()
                } else {
                    viewState?.toastError("Тип не обновлен")
                }
            }, {
                it.printStackTrace()
                viewState?.toastError("Ошибка обновления типа")
            })
            .addTo(compositeDisposable)
    }

}
