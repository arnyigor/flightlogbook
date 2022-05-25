package com.arny.flightlogbook.presentation.customfields.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arny.core.CONSTS.EXTRAS.EXTRA_ACTION_EDIT_CUSTOM_FIELD
import com.arny.core.CONSTS.EXTRAS.EXTRA_CUSTOM_FIELD_ID
import com.arny.core.CONSTS.REQUESTS.REQUEST_CUSTOM_FIELD
import com.arny.core.CONSTS.REQUESTS.REQUEST_CUSTOM_FIELD_EDIT
import com.arny.core.utils.ToastMaker
import com.arny.core.utils.getExtra
import com.arny.flightlogbook.R
import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.databinding.FragmentCustomFieldsListBinding
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.navigation.OpenDrawerListener
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class CustomFieldsListFragment : BaseMvpFragment(), CustomFieldsListView {
    private lateinit var binding: FragmentCustomFieldsListBinding
    private lateinit var customFieldsAdapter: CustomFieldsAdapter
    private val args: CustomFieldsListFragmentArgs by navArgs()
    private var openDrawerListener: OpenDrawerListener? = null

    @Inject
    lateinit var presenterProvider: Provider<CustomFieldsListPresenter>
    private val presenter by moxyPresenter { presenterProvider.get() }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is OpenDrawerListener) {
            openDrawerListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomFieldsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isRequestField = args.isRequestField
        title =
            if (isRequestField) getString(R.string.custom_field_select) else getString(R.string.custom_fields)
        initAdapter()
        openDrawerListener?.onChangeHomeButton(isRequestField)
        binding.fabAddCustomField.setOnClickListener {
            presenter.onFabClicked()
        }
        setFragmentResultListener(REQUEST_CUSTOM_FIELD_EDIT) { _, data ->
            if (data.getExtra<Boolean>(EXTRA_ACTION_EDIT_CUSTOM_FIELD) == true) {
                presenter.loadCustomFields()
            }
        }
    }

    private fun initAdapter() {
        customFieldsAdapter = CustomFieldsAdapter(onItemClick = (::onAdapterItemClick))
        binding.rvFieldsList.apply {
            adapter = customFieldsAdapter
        }
    }

    private fun onAdapterItemClick(item: CustomField) {
        if (args.isRequestField) {
            setFragmentResult(
                REQUEST_CUSTOM_FIELD,
                bundleOf(EXTRA_CUSTOM_FIELD_ID to item.id)
            )
            findNavController().popBackStack()
        } else {
            navigateToFieldEdit(item.id)
        }
    }

    override fun showProgress(show: Boolean) {
        binding.pbLoader.isVisible = show
    }

    override fun navigateToFieldEdit(id: Long?) {
        if (id != null) {
            findNavController().navigate(
                CustomFieldsListFragmentDirections.actionFieldsToCustomFieldEditFragment(
                    fieldId = id,
                    isRequest = true
                )
            )
        } else {
            findNavController().navigate(
                CustomFieldsListFragmentDirections.actionFieldsToCustomFieldEditFragment(
                    isRequest = true
                )
            )
        }
    }

    override fun showEmptyView(show: Boolean) {
        binding.tvEmptyView.isVisible = show
    }

    override fun showList(list: List<CustomField>) {
        customFieldsAdapter.submitList(list.toMutableList())
    }

    override fun toastError(message: String?) {
        ToastMaker.toastError(context, message)
    }
}
