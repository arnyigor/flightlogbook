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

    fun loadTimes() {
        fromCallable { repository.queryDBTimeTypes() }
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
        fromCallable { repository.updateDBTimeType(item.id, newName) }
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

    fun removeTimeType(item: TimeTypeEntity) {
        fromCallable { repository.removeDBTimeType(item.id) }
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
        fromCallable { repository.addDBTimeType(title) }
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

    fun onItemSelect(item: TimeTypeEntity, position: Int, items: ArrayList<TimeTypeEntity>?) {
        item.selected = !item.selected
        viewState?.notifyItemChanged(position)
        items?.let { list ->
            viewState?.setBtnConfirmSelectVisible(list.any { it.selected })
        }
    }

    fun onConfirmSelected(items: ArrayList<TimeTypeEntity>?) {
        val selected = items?.filter { it.selected }?.map { it.id }?.joinToString()
        viewState?.onConfirmSelectedTimes(selected)
    }
}
