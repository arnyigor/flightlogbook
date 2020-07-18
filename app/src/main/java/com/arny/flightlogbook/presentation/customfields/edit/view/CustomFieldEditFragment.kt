package com.arny.flightlogbook.presentation.customfields.edit.view


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import com.arny.flightlogbook.R
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.presentation.customfields.edit.presenter.CustomFieldsEditPresenter
import com.arny.flightlogbook.presentation.main.BackButtonListener
import com.arny.flightlogbook.presentation.main.MainActivity
import com.arny.flightlogbook.presentation.main.Router
import com.arny.helpers.utils.ToastMaker
import com.arny.helpers.utils.Utility
import kotlinx.android.synthetic.main.fragment_edit_custom_field_layout.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter


class CustomFieldEditFragment : MvpAppCompatFragment(), CustomFieldsEditView, BackButtonListener {
    private var router: Router? = null

    companion object {
        const val PARAM_FIELD_ID = "PARAM_FIELD_ID"

        @JvmStatic
        fun getInstance(bundle: Bundle? = null) = CustomFieldEditFragment().apply {
            bundle?.let {
                arguments = it
            }
        }
    }

    private lateinit var types: Array<String>

    @InjectPresenter
    lateinit var presenter: CustomFieldsEditPresenter

    @ProvidePresenter
    fun providePresenter() = CustomFieldsEditPresenter()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Router) {
            router = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getLong(CONSTS.EXTRAS.EXTRA_CUSTOM_FIELD_ID)?.let { presenter.setId(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_custom_field_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        types = resources.getStringArray(R.array.custom_fields_types)
        val activity = activity
        activity?.title = getString(R.string.edit)
        if (activity is MainActivity) {
            activity.lockNavigationDrawer()
        }
        tiedtCustomFieldName.doAfterTextChanged {
            if (tiedtCustomFieldName.isFocused) {
                presenter.setFieldName(it.toString())
            }
        }

        spinFieldType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                presenter.setType(position)
            }
        }

        btnSave.setOnClickListener {
            presenter.onSaveClicked()
        }
    }

    override fun onResultOk() {
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, null)
    }

    override fun showError(@StringRes strRes: Int) {
        ToastMaker.toastError(context, getString(strRes))
    }

    override fun onReturnBack() {
        val activity = activity
        if (activity is MainActivity) {
            activity.unLockNavigationDrawer()
        }
        router?.onBackPress()
    }

    override fun showResult(@StringRes strRes: Int) {
        ToastMaker.toastSuccess(context, getString(strRes))
    }

    override fun hideKeyboard() {
        Utility.hideSoftKeyboard(requireContext())
    }

    override fun showProgress(show: Boolean) {
        if (show) {
            clpbLoading.show()
        } else {
            clpbLoading.hide()
        }
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun setTitle(name: String?) {
        tiedtCustomFieldName.setText(name)
    }

    override fun setType(type: CustomFieldType?) {
        if (type != null) {
            spinFieldType.setSelection(types.indexOf(getString(type.getTypeName())))
        }
    }

    override fun showNameError(stringRes: Int?) {
        tiedtCustomFieldName.error = stringRes?.let { getString(it) }
    }

}
