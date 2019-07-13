package com.arny.flightlogbook.presentation.types

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.PlaneType
import com.arny.flightlogbook.data.source.MainRepositoryImpl
import com.arny.flightlogbook.data.utils.addTo
import com.arny.flightlogbook.data.utils.fromCallable
import com.arny.flightlogbook.data.utils.observeOnMain
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@InjectViewState
class PlaneTypesPresenter : MvpPresenter<PlaneTypesView>() {
    private val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var repository: MainRepositoryImpl

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: PlaneTypesView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    fun loadTypes() {
        fromCallable { repository.loadPlaneTypes() }
                .observeOnMain()
                .subscribe({
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
                    viewState?.toastError(it.message)
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    fun addType(name: String) {
        fromCallable { repository.addType(name) }
                .observeOnMain()
                .subscribe({
                    if (it) {
                        loadTypes()
                    } else {
                        viewState?.toastError(repository.getString(R.string.str_type_add_fail))
                    }
                }, {
                    it.printStackTrace()
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun removeType(item: PlaneType) {
        fromCallable { repository.removeType(item) }
                .observeOnMain()
                .subscribe({
                    if (it) {
                        loadTypes()
                    } else {
                        viewState?.toastError(repository.getString(R.string.str_type_remove_fail))
                    }
                }, {
                    it.printStackTrace()
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun removeAllPlaneTypes() {
        fromCallable { repository.removeTypes() }
                .observeOnMain()
                .subscribe({
                    if (it) {
                        viewState?.clearAdapter()
                        viewState?.setAdapterVisible(false)
                        viewState?.setEmptyViewVisible(true)
                        viewState?.setBtnRemoveAllVisible(false)
                    } else {
                        viewState?.toastError(repository.getString(R.string.str_types_removes_fail))
                    }
                }, {
                    it.printStackTrace()
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun updatePlaneTypeTitle(type: PlaneType, title: String?, position: Int) {
        fromCallable { repository.updatePlaneTypeTitle(type, title) }
                .observeOnMain()
                .subscribe({
                    if (it) {
                        type.typeName = title
                        viewState?.notifyItemChanged(position)
                    } else {
                        viewState?.toastError(repository.getString(R.string.str_type_change_fail))
                    }
                }, {
                    it.printStackTrace()
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

}