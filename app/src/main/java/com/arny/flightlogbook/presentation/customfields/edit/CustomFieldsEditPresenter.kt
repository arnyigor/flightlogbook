package com.arny.flightlogbook.presentation.customfields.edit

import com.arny.core.utils.fromSingle
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.customfields.domain.ICustomFieldInteractor
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class CustomFieldsEditPresenter : BaseMvpPresenter<CustomFieldsEditView>() {

    init {
        FlightApp.appComponent.inject(this)
    }

    private var addTime: Boolean = false
    private var name: String? = null
    private var type: CustomFieldType = CustomFieldType.None
    private var showByDefault: Boolean = false

    private val uiTypes = CustomFieldType.values()

    @Inject
    lateinit var customFieldInteractor: ICustomFieldInteractor

    var fieldId: Long? = null
        set(value) {
            field = if (value != 0L) value else null
        }

    override fun onFirstViewAttach() {
        if (fieldId != null && fieldId != 0L) {
            fromSingle { customFieldInteractor.getCustomField(fieldId!!) }
                    .subscribeFromPresenter({
                        val customField = it.value
                        if (customField != null) {
                            name = customField.name
                            viewState.setTitle(name)
                            type = customField.type
                            viewState.setType(type)
                            showByDefault = customField.showByDefault
                            viewState.setDefaultChecked(showByDefault)
                            addTime = customField.addTime
                            viewState.setAddTimeChecked(addTime)
                        }
                    }, {
                        it.printStackTrace()
                    })
        }
    }

    fun onSaveClicked(checkedAddTime: Boolean) {
        addTime = checkedAddTime
        if (name.isNullOrBlank()) {
            viewState.showNameError(R.string.error_empty_text_field)
            return
        }
        viewState.showProgress(false)
        fromSingle { customFieldInteractor.save(fieldId, name!!, type, showByDefault, addTime) }
                .subscribeFromPresenter({
                    viewState.showProgress(false)
                    viewState.showResult(R.string.save_custom_field_success)
                    viewState.onResultOk()
                }, {
                    viewState.showProgress(false)
                    viewState.showError(R.string.error_custom_field_not_saved)
                })
    }

    fun setFieldName(name: String) {
        this.name = name
    }

    fun setType(position: Int) {
        type = uiTypes.getOrNull(position) ?: CustomFieldType.None
        viewState.setChBoxAddTimeVisible(type is CustomFieldType.Time)
    }

    fun setDefaultChecked(checked: Boolean) {
        showByDefault = checked
    }

    fun setAddTimeChecked(checked: Boolean) {
        addTime = checked
    }

    fun onDelete() {
        fieldId?.let {
            fromSingle { customFieldInteractor.removeField(it) }
                    .subscribeFromPresenter({
                        if (it) {
                            viewState.showResult(R.string.custom_field_removed)
                            viewState.onResultOk()
                        } else {
                            viewState.showError(R.string.error_custom_field_not_removed)
                        }
                    }, {
                        viewState.showError(R.string.error_custom_field_not_removed)
                    })
        }
    }
}
