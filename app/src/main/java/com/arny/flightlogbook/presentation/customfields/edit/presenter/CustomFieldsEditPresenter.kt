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
    private val uiTypes = listOf(
            CustomFieldType.TYPE_NUMBER_INT,
            CustomFieldType.TYPE_BOOLEAN
    )

    @Inject
    lateinit var customFieldInteractor: ICustomFieldInteractor

    private var id: Long? = null

    fun setId(id: Long?) {
        this.id = id
    }

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        id?.let { fieldId ->
            customFieldInteractor.getCustomField(fieldId)
                    .subscribeFromPresenter({
                        val customField = it.value
                        if (customField != null) {
                            name = customField.name
                            viewState.setTitle(name)
                            type = customField.type
                            viewState.setType(type)
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
        customFieldInteractor.save(id, name, type)
        viewState.onResultOk()
    }

    fun setFieldName(name: String) {
        this.name = name
    }

    fun setType(position: Int) {
        type = uiTypes.getOrNull(position) ?: CustomFieldType.TYPE_NONE
    }

}
