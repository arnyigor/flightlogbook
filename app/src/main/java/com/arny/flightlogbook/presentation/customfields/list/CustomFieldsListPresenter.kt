package com.arny.flightlogbook.presentation.customfields.list

import com.arny.core.utils.fromSingle
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.customfields.domain.ICustomFieldInteractor
import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
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
        fromSingle { customFieldInteractor.getCustomFields() }
                .subscribeFromPresenter({
                    viewState.showProgress(false)
                    viewState.showEmptyView(it.isEmpty())
                    viewState.showList(it)
                }, {
                    viewState.showProgress(false)
                    viewState.toastError(it.message)
                })
    }

}
