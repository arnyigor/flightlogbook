package com.arny.flightlogbook.presentation.customfields.edit.presenter

import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.customfields.domain.ICustomFieldInteractor
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import com.arny.flightlogbook.presentation.customfields.edit.view.CustomFieldsEditView
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class CustomFieldsEditPresenter : BaseMvpPresenter<CustomFieldsEditView>() {

    private var name: String? = null
    private var type: CustomFieldType = CustomFieldType.TYPE_NONE
    private var showByDefault: Boolean = false
    private val uiTypes = listOf(
            CustomFieldType.TYPE_TEXT,
            CustomFieldType.TYPE_NUMBER,
            CustomFieldType.TYPE_TIME,
            CustomFieldType.TYPE_BOOLEAN,
            CustomFieldType.TYPE_NONE
    )

    @Inject
    lateinit var customFieldInteractor: ICustomFieldInteractor

    private var id: Long? = null

    fun setId(id: Long?) {
        if (id != null && id != 0L) {
            this.id = id
        }
    }

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        if (id != null && id != 0L) {
            customFieldInteractor.getCustomField(id!!)
                    .subscribeFromPresenter({
                        val customField = it.value
                        if (customField != null) {
                            name = customField.name
                            viewState.setTitle(name)
                            type = customField.type
                            viewState.setType(type)
                            showByDefault = customField.showByDefault
                            viewState.setDefaultChecked(showByDefault)
                        }
                    }, {
                        it.printStackTrace()
                    })
        }

    }

    fun onSaveClicked() {
        if (name.isNullOrBlank()) {
            viewState.showNameError(R.string.error_empty_text_field)
            return
        }
        viewState.showProgress(false)
        customFieldInteractor.save(id, name!!, type, showByDefault)
                .subscribeFromPresenter({
                    viewState.showProgress(false)
                    viewState.showResult(R.string.save_custom_field_success)
                    viewState.onResultOk()
                    viewState.onReturnBack()
                }, {
                    viewState.showProgress(false)
                    viewState.showError(R.string.error_custom_field_not_saved)
                })
    }

    fun setFieldName(name: String) {
        this.name = name
    }

    fun setType(position: Int) {
        type = uiTypes.getOrNull(position) ?: CustomFieldType.TYPE_NONE
    }

    fun setDefaultChecked(checked: Boolean) {
        showByDefault = checked
    }

}
