package com.arny.flightlogbook.presentation.customfields.edit

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.AbstractArrayAdapter
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.presentation.main.BackButtonListener
import com.arny.flightlogbook.presentation.main.MainActivity
import com.arny.flightlogbook.presentation.main.Router
import com.arny.helpers.utils.KeyboardHelper.hideKeyboard
import com.arny.helpers.utils.ToastMaker
import kotlinx.android.synthetic.main.fragment_edit_custom_field_layout.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class CustomFieldEditFragment : MvpAppCompatFragment(), CustomFieldsEditView, BackButtonListener {
    private var router: Router? = null

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
        if (context is Router) {
            router = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.getLong(CONSTS.EXTRAS.EXTRA_CUSTOM_FIELD_ID)?.let { presenter.setId(it) }
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
        }
        return true
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
