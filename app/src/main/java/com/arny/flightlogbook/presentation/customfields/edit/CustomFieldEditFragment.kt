package com.arny.flightlogbook.presentation.customfields.edit

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import com.arny.core.CONSTS
import com.arny.core.utils.KeyboardHelper.hideKeyboard
import com.arny.core.utils.ToastMaker
import com.arny.core.utils.alertDialog
import com.arny.core.utils.getExtra
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.AbstractArrayAdapter
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.databinding.FragmentEditCustomFieldLayoutBinding
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.main.AppRouter
import moxy.ktx.moxyPresenter

class CustomFieldEditFragment : BaseMvpFragment(), CustomFieldsEditView {
    private lateinit var binding: FragmentEditCustomFieldLayoutBinding
    private var appRouter: AppRouter? = null

    companion object {
        fun getInstance(bundle: Bundle? = null) = CustomFieldEditFragment().apply {
            bundle?.let {
                arguments = it
            }
        }
    }

    private lateinit var types: Array<String>
    private val presenter by moxyPresenter { CustomFieldsEditPresenter() }

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
        inflater.inflate(R.menu.add_edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                hideKeyboard(requireActivity())
                presenter.onSaveClicked(binding.chbAddTime.isChecked)
            }
            R.id.action_remove -> {
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

    override fun getTitle(): String = getString(R.string.edit)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditCustomFieldLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        types = resources.getStringArray(R.array.custom_fields_types)
        binding.tiedtCustomFieldName.doAfterTextChanged {
            if (binding.tiedtCustomFieldName.isFocused) {
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
        binding.spinFieldType.adapter = abstractArrayAdapter
        binding.spinFieldType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                presenter.setType(binding.spinFieldType.selectedItemPosition)
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                presenter.setType(binding.spinFieldType.selectedItemPosition)
            }
        }

        binding.chbDefault.setOnCheckedChangeListener { _, isChecked ->
            presenter.setDefaultChecked(isChecked)
        }

        binding.chbAddTime.setOnCheckedChangeListener { _, isChecked ->
            presenter.setAddTimeChecked(isChecked)
        }
    }

    override fun onResultOk() {
        setFragmentResult(
            CONSTS.REQUESTS.REQUEST_CUSTOM_FIELD_EDIT,
            bundleOf(CONSTS.EXTRAS.EXTRA_ACTION_EDIT_CUSTOM_FIELD to true)
        )
        requireActivity().onBackPressed()
    }

    override fun showError(@StringRes strRes: Int) {
        ToastMaker.toastError(context, getString(strRes))
    }

    override fun showResult(@StringRes strRes: Int) {
        ToastMaker.toastSuccess(context, getString(strRes))
    }

    override fun showProgress(show: Boolean) {
        if (show) {
            binding.clpbLoading.show()
        } else {
            binding.clpbLoading.hide()
        }
    }

    override fun setTitle(name: String?) {
        binding.tiedtCustomFieldName.setText(name)
    }

    override fun setType(type: CustomFieldType?) {
        if (type != null) {
            val indexOf = types.indexOf(getString(type.nameRes))
            binding.spinFieldType.setSelection(indexOf)
        }
    }

    override fun showNameError(stringRes: Int?) {
        binding.tiedtCustomFieldName.error = stringRes?.let { getString(it) }
    }

    override fun setDefaultChecked(showByDefault: Boolean) {
        binding.chbDefault.isChecked = showByDefault
    }

    override fun setAddTimeChecked(checked: Boolean) {
        binding.chbAddTime.isChecked = checked
    }

    override fun setChBoxAddTimeVisible(visible: Boolean) {
        binding.chbAddTime.isVisible = visible
    }
}
