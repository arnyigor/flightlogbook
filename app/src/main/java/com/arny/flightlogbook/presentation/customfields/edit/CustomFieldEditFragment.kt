package com.arny.flightlogbook.presentation.customfields.edit

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.AbstractArrayAdapter
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.main.AppRouter
import com.arny.flightlogbook.presentation.main.BackButtonListener
import com.arny.helpers.utils.KeyboardHelper.hideKeyboard
import com.arny.helpers.utils.ToastMaker
import com.arny.helpers.utils.alertDialog
import com.arny.helpers.utils.getExtra
import kotlinx.android.synthetic.main.fragment_edit_custom_field_layout.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class CustomFieldEditFragment : BaseMvpFragment(), CustomFieldsEditView, BackButtonListener {
    private var appRouter: AppRouter? = null

    companion object {
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
        if (context is AppRouter) {
            appRouter = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        presenter.fieldId = getExtra(CONSTS.EXTRAS.EXTRA_CUSTOM_FIELD_ID)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.custom_fields_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                hideKeyboard(requireActivity())
                presenter.onSaveClicked(chbAddTime.isChecked)
            }
            R.id.action_delete -> {
                hideKeyboard(requireActivity())
                alertDialog(
                        context = requireContext(),
                        title = getString(R.string.str_delete),
                        btnCancelText = getString(R.string.str_cancel),
                        onConfirm = { presenter.onDelete() }
                )
            }
        }
        return true
    }

    override fun getLayoutId(): Int = R.layout.fragment_edit_custom_field_layout

    override fun getTitle(): String? = getString(R.string.edit)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        types = resources.getStringArray(R.array.custom_fields_types)
        tiedtCustomFieldName.doAfterTextChanged {
            if (tiedtCustomFieldName.isFocused) {
                presenter.setFieldName(it.toString())
            }
        }
        val map = CustomFieldType.values().map { getString(it.nameRes) }.toTypedArray()
        val abstractArrayAdapter = object : AbstractArrayAdapter<String>(
                context,
                android.R.layout.simple_list_item_1,
                map
        ) {
            override fun getItemTitle(item: String?) = item
        }
        spinFieldType.adapter = abstractArrayAdapter
        spinFieldType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                presenter.setType(spinFieldType.selectedItemPosition)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                presenter.setType(spinFieldType.selectedItemPosition)
            }
        }

        chbDefault.setOnCheckedChangeListener { _, isChecked ->
            presenter.setDefaultChecked(isChecked)
        }

        chbAddTime.setOnCheckedChangeListener { _, isChecked ->
            presenter.setAddTimeChecked(isChecked)
        }
    }

    override fun onResultOk() {
        appRouter?.setResultToTargetFragment(this@CustomFieldEditFragment)
    }

    override fun showError(@StringRes strRes: Int) {
        ToastMaker.toastError(context, getString(strRes))
    }

    override fun showResult(@StringRes strRes: Int) {
        ToastMaker.toastSuccess(context, getString(strRes))
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
            val element = getString(type.nameRes)
            val indexOf = types.indexOf(element)
            spinFieldType.setSelection(indexOf)
        }
    }

    override fun showNameError(stringRes: Int?) {
        tiedtCustomFieldName.error = stringRes?.let { getString(it) }
    }

    override fun setDefaultChecked(showByDefault: Boolean) {
        chbDefault.isChecked = showByDefault
    }

    override fun setAddTimeChecked(checked: Boolean) {
        chbAddTime.isChecked = checked
    }

    override fun setChBoxAddTimeVisible(visible: Boolean) {
        chbAddTime.isVisible = visible
    }
}
