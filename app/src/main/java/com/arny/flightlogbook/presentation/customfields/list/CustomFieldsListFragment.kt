package com.arny.flightlogbook.presentation.customfields.list


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.core.CONSTS
import com.arny.core.CONSTS.EXTRAS.EXTRA_CUSTOM_FIELD_ID
import com.arny.core.CONSTS.REQUESTS.REQUEST_EDIT_CUSTOM_FIELD
import com.arny.core.utils.ToastMaker
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.main.AppRouter
import com.arny.flightlogbook.presentation.main.NavigateItems
import kotlinx.android.synthetic.main.fragment_custom_fields_list.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class CustomFieldsListFragment : BaseMvpFragment(), CustomFieldsListView {

    companion object {
        fun getInstance(bundle: Bundle? = null) = CustomFieldsListFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private var reload: Boolean = false
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

    override fun getLayoutId(): Int = R.layout.fragment_custom_fields_list

    override fun getTitle(): String? = getString(title)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isRequestField = arguments?.getBoolean(CONSTS.REQUESTS.REQUEST) == true
        title = if (isRequestField) R.string.custom_field_select else R.string.custom_fields
        updateTitle()
        customFieldsAdapter = CustomFieldsAdapter()
        rvFieldsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = customFieldsAdapter
        }
        customFieldsAdapter.setViewHolderListener(object : SimpleAbstractAdapter.OnViewHolderListener<CustomField> {
            override fun onItemClick(position: Int, item: CustomField) {
                if (isRequestField) {
                    appRouter?.setResultToTargetFragment(this@CustomFieldsListFragment, Intent().apply {
                        putExtra(EXTRA_CUSTOM_FIELD_ID, item.id)
                    })
                } else {
                    presenter.onItemClick(item)
                }
            }
        })
        fabAddCustomField.setOnClickListener {
            presenter.onFabClicked()
        }
    }

    override fun onResume() {
        super.onResume()
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
                bundleOf(
                        EXTRA_CUSTOM_FIELD_ID to id,
                        CONSTS.REQUESTS.REQUEST to true
                ),
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
