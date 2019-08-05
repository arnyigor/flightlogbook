package com.arny.flightlogbook.presentation.timetypes

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.domain.models.TimeType
import com.arny.domain.times.TimeTypesUseCase
import com.arny.flightlogbook.FlightApp
import com.arny.helpers.utils.addTo
import com.arny.helpers.utils.observeOnMain
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


@InjectViewState
class TimesListPresenter : MvpPresenter<TimesListView>() {
    private val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var timeTypesUseCase: TimeTypesUseCase

    init {
        FlightApp.appComponent.inject(this)
    }


    override fun detachView(view: TimesListView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    fun loadTimes() {
        viewState?.setEmptyView(false)
        timeTypesUseCase.queryTimeTypes()
                .observeOnMain()
                .subscribe({
                    viewState?.updateAdapter(it)
                    viewState?.setEmptyView(it.isEmpty())
                }, {
                    it.printStackTrace()
                    viewState?.setEmptyView(true)
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun editTimeTypeTitle(item: TimeType, newName: String, position: Int) {
        timeTypesUseCase.updateTimeType(item.id, newName)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        item.title = newName
                        viewState?.notifyItemChanged(position)
                    } else {
                        viewState?.toastError("Название времени не изменено")
                    }
                }, {
                    it.printStackTrace()
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun removeTimeType(item: TimeType) {
        timeTypesUseCase.removeTimeType(item.id)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        loadTimes()
                    } else {
                        viewState?.toastError("Время не удалено")
                    }
                }, {
                    it.printStackTrace()
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun addTimeType(title: String?) {
        timeTypesUseCase.addTimeType(title)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        loadTimes()
                    } else {
                        viewState?.toastError("Время не добавлено")
                    }
                }, {
                    it.printStackTrace()
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun onItemClick(item: TimeType) {
        viewState?.showDialogSetTime(item)
    }

    fun addFlightTime(id: Long?, title: String?, totalTime: Int, addToFlight: Boolean) {
        viewState?.confirmSelectedTimeFlight(id, title, totalTime, addToFlight)
    }
}
