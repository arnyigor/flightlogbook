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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.CONSTS
import com.arny.flightlogbook.data.models.CustomFieldType
import com.arny.flightlogbook.databinding.FragmentEditCustomFieldLayoutBinding
import com.arny.flightlogbook.presentation.mvp.BaseMvpFragment
import com.arny.flightlogbook.presentation.utils.AbstractArrayAdapter
import com.arny.flightlogbook.presentation.utils.KeyboardHelper.hideKeyboard
import com.arny.flightlogbook.presentation.utils.ToastMaker
import com.arny.flightlogbook.presentation.utils.alertDialog
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class CustomFieldEditFragment : BaseMvpFragment(), CustomFieldsEditView {
    private val args: CustomFieldEditFragmentArgs by navArgs()
    private lateinit var binding: FragmentEditCustomFieldLayoutBinding

    @Inject
    lateinit var presenterProvider: Provider<CustomFieldsEditPresenter>
    private val presenter by moxyPresenter { presenterProvider.get() }
    private lateinit var types: Array<String>

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        presenter.fieldId = args.fieldId
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            R.id.action_save -> {
                hideKeyboard(requireActivity())
                presenter.onSaveClicked(binding.chbAddTime.isChecked)
                true
            }
            R.id.action_remove -> {
                hideKeyboard(requireActivity())
                alertDialog(
                    context = requireContext(),
                    title = getString(R.string.str_delete),
                    btnCancelText = getString(R.string.str_cancel),
                    onConfirm = { presenter.onDelete() }
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

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
        initAdapter()
        binding.chbDefault.setOnCheckedChangeListener { _, isChecked ->
            presenter.setDefaultChecked(isChecked)
        }
        binding.chbAddTime.setOnCheckedChangeListener { _, isChecked ->
            presenter.setAddTimeChecked(isChecked)
        }
    }

    private fun initAdapter() {
        binding.spinFieldType.adapter = object : AbstractArrayAdapter<String>(
            context,
            android.R.layout.simple_list_item_1,
            CustomFieldType.values().map { getString(it.nameRes) }.toTypedArray()
        ) {
            override fun getItemTitle(item: String?) = item
        }
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
    }

    override fun updateTitle(@StringRes titleRes: Int) {
        title = getString(titleRes)
    }

    override fun onResultOk() {
        setFragmentResult(
            CONSTS.REQUESTS.REQUEST_CUSTOM_FIELD_EDIT,
            bundleOf(CONSTS.EXTRAS.EXTRA_ACTION_EDIT_CUSTOM_FIELD to args.isRequest)
        )
        findNavController().popBackStack()
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

    override fun setName(name: String?) {
        binding.tiedtCustomFieldName.setText(name)
    }

    override fun setType(type: CustomFieldType?) {
        if (type != null) {
            binding.spinFieldType.setSelection(types.indexOf(getString(type.nameRes)))
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
