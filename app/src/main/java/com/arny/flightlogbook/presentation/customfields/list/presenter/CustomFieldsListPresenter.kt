package com.arny.flightlogbook.presentation.customfields.list.presenter

import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.customfields.domain.ICustomFieldInteractor
import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import com.arny.flightlogbook.presentation.customfields.list.view.CustomFieldsListView
import moxy.InjectViewState
import javax.inject.Inject


@InjectViewState
class CustomFieldsListPresenter : BaseMvpPresenter<CustomFieldsListView>() {
    @Inject
    lateinit var customFieldInteractor: ICustomFieldInteractor


    init {
        FlightApp.appComponent.inject(this)
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadCustomFields()
    }

    fun onItemClick(item: CustomField) {
        viewState.navigateToFieldEdit(item.id)
    }

    fun onFabClicked() {
        viewState.navigateToFieldEdit()
    }

    fun loadCustomFields() {
        viewState.showProgress(true)
        customFieldInteractor.getCustomFields()
                .subscribeFromPresenter({
                    viewState.showProgress(false)
                    viewState.showEmptyView(it.isEmpty())
                    if (it.isNotEmpty()) {
                        viewState.showList(it)
                    }
                }, {
                    viewState.showProgress(false)
                    it.printStackTrace()// TODO: 17.07.2020 убрать
                    viewState.toastError(it.message)
                })
    }

}
