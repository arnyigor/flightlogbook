package com.arny.flightlogbook.presentation.customfields.list

import com.arny.flightlogbook.domain.customfields.ICustomFieldInteractor
import com.arny.flightlogbook.presentation.mvp.BaseMvpPresenter
import io.reactivex.Single
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class CustomFieldsListPresenter @Inject constructor(
    private val customFieldInteractor: ICustomFieldInteractor
) : BaseMvpPresenter<CustomFieldsListView>() {
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadCustomFields()
    }

    fun onFabClicked() {
        viewState.navigateToFieldEdit()
    }

    fun loadCustomFields() {
        viewState.showProgress(true)
        Single.fromCallable { customFieldInteractor.getCustomFields() }
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
