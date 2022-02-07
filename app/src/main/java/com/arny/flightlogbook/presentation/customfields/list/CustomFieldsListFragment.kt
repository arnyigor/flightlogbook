package com.arny.flightlogbook.presentation.customfields.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.core.CONSTS
import com.arny.core.CONSTS.EXTRAS.EXTRA_CUSTOM_FIELD_ID
import com.arny.core.CONSTS.REQUESTS.REQUEST_CUSTOM_FIELD
import com.arny.core.CONSTS.REQUESTS.REQUEST_EDIT_CUSTOM_FIELD
import com.arny.core.utils.ToastMaker
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.databinding.FragmentCustomFieldsListBinding
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.main.AppRouter
import com.arny.flightlogbook.presentation.main.NavigateItems
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class CustomFieldsListFragment : BaseMvpFragment(), CustomFieldsListView {
    companion object {
        fun getInstance(bundle: Bundle? = null) = CustomFieldsListFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private lateinit var binding: FragmentCustomFieldsListBinding
    private var title: Int = R.string.custom_fields
    private lateinit var customFieldsAdapter: CustomFieldsAdapter

    @InjectPresenter
    lateinit var presenter: CustomFieldsListPresenter

    @ProvidePresenter
    fun providePresenter() = CustomFieldsListPresenter()

    private var appRouter: AppRouter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AppRouter) {
            appRouter = context
        }
    }

    override fun getTitle(): String = getString(title)

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
        val isRequestField = arguments?.getBoolean(CONSTS.REQUESTS.REQUEST) == true
        title = if (isRequestField) R.string.custom_field_select else R.string.custom_fields
        updateTitle()
        customFieldsAdapter = CustomFieldsAdapter()
        binding.rvFieldsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = customFieldsAdapter
        }
        customFieldsAdapter.setViewHolderListener(object :
            SimpleAbstractAdapter.OnViewHolderListener<CustomField> {
            override fun onItemClick(position: Int, item: CustomField) {
                if (isRequestField) {
                    setFragmentResult(
                        REQUEST_CUSTOM_FIELD,
                        bundleOf(EXTRA_CUSTOM_FIELD_ID to item.id)
                    )
                    requireActivity().onBackPressed()
                } else {
                    presenter.onItemClick(item)
                }
            }
        })
        binding.fabAddCustomField.setOnClickListener {
            presenter.onFabClicked()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.loadCustomFields()
    }

    override fun showProgress(show: Boolean) {
        binding.pbLoader.isVisible = show
    }

    override fun navigateToFieldEdit(id: Long?) {
        appRouter?.navigateTo(
            NavigateItems.ITEM_EDIT_FIELD,
            true,
            bundleOf(
                EXTRA_CUSTOM_FIELD_ID to id,
                CONSTS.REQUESTS.REQUEST to true
            ),
            requestCode = REQUEST_EDIT_CUSTOM_FIELD,
            targetFragment = this@CustomFieldsListFragment
        )
    }

    override fun showEmptyView(show: Boolean) {
        binding.tvEmptyView.isVisible = show
    }

    override fun showList(list: List<CustomField>) {
        customFieldsAdapter.addAll(list)
    }

    override fun toastError(message: String?) {
        ToastMaker.toastError(context, message)
    }
}
