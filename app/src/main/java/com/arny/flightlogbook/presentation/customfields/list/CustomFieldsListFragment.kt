package com.arny.flightlogbook.presentation.customfields.list


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.constants.CONSTS.EXTRAS.EXTRA_CUSTOM_FIELD_ID
import com.arny.flightlogbook.constants.CONSTS.REQUESTS.REQUEST_EDIT_CUSTOM_FIELD
import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.presentation.common.FragmentContainerActivity
import com.arny.flightlogbook.presentation.main.AppRouter
import com.arny.flightlogbook.presentation.main.NavigateItems
import com.arny.helpers.utils.ToastMaker
import kotlinx.android.synthetic.main.fragment_custom_fields_list.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class CustomFieldsListFragment : MvpAppCompatFragment(), CustomFieldsListView {

    companion object {
        private const val PARAM_RELOAD = "PARAM_RELOAD"
        private const val PARAM_REQUEST = "PARAM_REQUEST"

        @JvmStatic
        fun getInstance(reload: Boolean = false, request: Boolean = false) =
                CustomFieldsListFragment().apply {
                    arguments = bundleOf(
                            PARAM_RELOAD to reload,
                            PARAM_REQUEST to request
                    )
                }
    }

    private var reload: Boolean = false
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_custom_fields_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isRequestField = arguments?.getBoolean(PARAM_REQUEST) == true
        requireActivity().title = getString(if (isRequestField) R.string.custom_field_select else R.string.custom_fields)
        customFieldsAdapter = CustomFieldsAdapter()
        rvFieldsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = customFieldsAdapter
        }
        customFieldsAdapter.setViewHolderListener(object : SimpleAbstractAdapter.OnViewHolderListener<CustomField> {
            override fun onItemClick(position: Int, item: CustomField) {
                if (isRequestField) {
                    val requireActivity = requireActivity()
                    if (requireActivity is FragmentContainerActivity) {
                        requireActivity.onSuccess(Intent().apply {
                            putExtra(EXTRA_CUSTOM_FIELD_ID, item.id)
                        })
                        requireActivity.onBackPressed()
                    }
                } else {
                    presenter.onItemClick(item)
                }
            }
        })
        fabAddCustomField.setOnClickListener {
            presenter.onFabClicked()
        }
        if (reload) {
            presenter.loadCustomFields()
            reload = false
        }
    }

    override fun showProgress(show: Boolean) {
        pbLoader.isVisible = show
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_EDIT_CUSTOM_FIELD -> {
                    reload = true
                }
            }
        }
    }

    override fun navigateToFieldEdit(id: Long?) {
        appRouter?.navigateTo(
                NavigateItems.ITEM_EDIT_FIELD,
                true,
                bundleOf(EXTRA_CUSTOM_FIELD_ID to id),
                requestCode = REQUEST_EDIT_CUSTOM_FIELD,
                targetFragment = this@CustomFieldsListFragment
        )
    }

    override fun showEmptyView(show: Boolean) {
        tvEmptyView.isVisible = show
    }

    override fun showList(list: List<CustomField>) {
        customFieldsAdapter.addAll(list)
    }

    override fun toastError(message: String?) {
        ToastMaker.toastError(context, message)
    }
}
