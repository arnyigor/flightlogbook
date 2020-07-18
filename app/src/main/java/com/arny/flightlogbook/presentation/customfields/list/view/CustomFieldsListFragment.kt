package com.arny.flightlogbook.presentation.customfields.list.view


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
import com.arny.flightlogbook.presentation.customfields.list.presenter.CustomFieldsListPresenter
import com.arny.flightlogbook.presentation.main.NavigateItems
import com.arny.flightlogbook.presentation.main.Router
import com.arny.helpers.utils.ToastMaker
import kotlinx.android.synthetic.main.fragment_custom_fields_list.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class CustomFieldsListFragment : MvpAppCompatFragment(), CustomFieldsListView {

    companion object {
        const val PARAM_RELOAD = "PARAM_RELOAD"

        @JvmStatic
        fun getInstance(reload: Boolean = false): CustomFieldsListFragment {
            val fragment = CustomFieldsListFragment()
            val bundle = Bundle()
            bundle.putBoolean(PARAM_RELOAD, reload)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var reload: Boolean = false
    private lateinit var customFieldsAdapter: CustomFieldsAdapter

    private var router: Router? = null

    @InjectPresenter
    lateinit var presenter: CustomFieldsListPresenter

    @ProvidePresenter
    fun providePresenter() = CustomFieldsListPresenter()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Router) {
            router = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_custom_fields_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.custom_fields)
        customFieldsAdapter = CustomFieldsAdapter()
        rvFieldsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = customFieldsAdapter
        }
        customFieldsAdapter.setViewHolderListener(object : SimpleAbstractAdapter.OnViewHolderListener<CustomField> {
            override fun onItemClick(position: Int, item: CustomField) {
                presenter.onItemClick(item)
            }
        })
        fabAddCustomField.setOnClickListener {
            presenter.onFabClicked()
        }
        if (reload) {
            presenter.loadCustomFields()
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
        router?.navigateTo(
                NavigateItems.ITEM_EDIT_FIELD,
                true,
                bundleOf(EXTRA_CUSTOM_FIELD_ID to id),
                requestCode = REQUEST_EDIT_CUSTOM_FIELD,
                targetFragment = this
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
