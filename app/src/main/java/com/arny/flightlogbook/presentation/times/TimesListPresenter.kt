package com.arny.flightlogbook.presentation.times

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.data.db.intities.TimeTypeEntity
import com.arny.flightlogbook.data.source.MainRepositoryImpl
import com.arny.flightlogbook.data.utils.addTo
import com.arny.flightlogbook.data.utils.fromCallable
import com.arny.flightlogbook.data.utils.observeOnMain
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


@InjectViewState
class TimesListPresenter : MvpPresenter<TimesListView>() {
    private val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var repository: MainRepositoryImpl

    init {
        FlightApp.appComponent.inject(this)
    }

    private fun getView(): TimesListView? {
        return viewState
    }

    override fun detachView(view: TimesListView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    fun loadTimes(flightId: Long?) {
        fromCallable { repository.queryDBFlightTimes(flightId) }
                .observeOnMain()
                .subscribe({
                    if (it.isNotEmpty()) {
                        getView()?.setEmptyView(false)
                        getView()?.setListVisible(true)
                        getView()?.updateAdapter(it)
                    } else {
                        getView()?.setEmptyView(true)
                        getView()?.setListVisible(false)
                    }
                }, {
                    it.printStackTrace()
                    getView()?.setEmptyView(true)
                    getView()?.setListVisible(false)
                    getView()?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun editTimeTypeTitle(item: TimeTypeEntity, newName: String, position: Int) {

    }

    fun removeTimeType(item: TimeTypeEntity) {

    }

    fun addTimeType(title: String?) {

    }
}
