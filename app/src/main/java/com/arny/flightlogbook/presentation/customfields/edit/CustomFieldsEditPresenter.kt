package com.arny.flightlogbook.presentation.customfields.edit

import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.customfields.domain.ICustomFieldInteractor
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class CustomFieldsEditPresenter : BaseMvpPresenter<CustomFieldsEditView>() {

    private var addTime: Boolean = false
    private var name: String? = null
    private var type: CustomFieldType = CustomFieldType.None
    private var showByDefault: Boolean = false
    private val uiTypes = CustomFieldType.values()

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
        customFieldInteractor.save(id, name!!, type, showByDefault, addTime)
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
        type = uiTypes.getOrNull(position) ?: CustomFieldType.None
        viewState.setChBoxAddTimeVisible(type is CustomFieldType.Time)
    }

    fun setDefaultChecked(checked: Boolean) {
        showByDefault = checked
    }

    fun setAddTimeChecked(checked: Boolean) {
        addTime = checked
    }
}
