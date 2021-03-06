package com.arny.flightlogbook.presentation.planetypes.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.core.CONSTS
import com.arny.core.utils.ToastMaker
import com.arny.core.utils.alertDialog
import com.arny.domain.models.PlaneType
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.main.AppRouter
import com.arny.flightlogbook.presentation.main.NavigateItems
import kotlinx.android.synthetic.main.plane_types_layout.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class PlaneTypesFragment : BaseMvpFragment(), PlaneTypesView, View.OnClickListener {
    companion object {
        fun getInstance(bundle: Bundle? = null) = PlaneTypesFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private var adapter: PlaneTypesAdapter? = null

    @InjectPresenter
    lateinit var typeListPresenter: PlaneTypesPresenter

    @ProvidePresenter
    fun provideTypeListPresenter() = PlaneTypesPresenter()

    private var appRouter: AppRouter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AppRouter) {
            appRouter = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun getLayoutId(): Int = R.layout.plane_types_layout
    override fun getTitle(): String? = getString(R.string.str_airplane_types)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isRequestField = arguments?.getBoolean(CONSTS.REQUESTS.REQUEST) == true
        rv_plane_types.layoutManager = LinearLayoutManager(context)
        rv_plane_types.itemAnimator = DefaultItemAnimator()
        adapter = PlaneTypesAdapter(
                false,
                typesListener = object : PlaneTypesAdapter.PlaneTypesListener {
                    override fun onEditType(position: Int, item: PlaneType) {
                        appRouter?.navigateTo(
                                NavigateItems.PLANE_TYPE_EDIT,
                                true,
                                bundleOf(CONSTS.EXTRAS.EXTRA_PLANE_TYPE_ID to item.typeId),
                                requestCode = CONSTS.REQUESTS.REQUEST_EDIT_PLANE_TYPE,
                                targetFragment = this@PlaneTypesFragment
                        )
                    }

                    override fun onDeleteType(position: Int, item: PlaneType) {
                        showRemoveDialog(item)
                    }

                    override fun onItemClick(position: Int, item: PlaneType) {
                        if (isRequestField) {
                            appRouter?.setResultToTargetFragment(this@PlaneTypesFragment, Intent().apply {
                                putExtra(CONSTS.EXTRAS.EXTRA_PLANE_TYPE_ID, item.typeId)
                            })
                        }
                    }
                })
        rv_plane_types.adapter = adapter
        fab_add_plane_type.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        typeListPresenter.loadTypes()
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(context, msg)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun setEmptyViewVisible(vis: Boolean) {
        tv_no_plane_types.isVisible = vis
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab_add_plane_type -> {
                appRouter?.navigateTo(
                        NavigateItems.PLANE_TYPE_EDIT,
                        true,
                        requestCode = CONSTS.REQUESTS.REQUEST_EDIT_PLANE_TYPE,
                        targetFragment = this@PlaneTypesFragment
                )
            }
        }
    }

    private fun showRemoveDialog(item: PlaneType) {
        alertDialog(
                requireActivity(),
                "${getString(R.string.str_delete)} ${item.typeName}?",
                null,
                getString(R.string.str_ok),
                getString(R.string.str_cancel),
                false,
                onConfirm = {
                    typeListPresenter.removeType(item)
                })
    }

    override fun notifyItemChanged(position: Int) {
        adapter?.notifyItemChanged(position)
    }

    override fun updateAdapter(list: List<PlaneType>) {
        adapter?.addAll(list)
    }

    override fun toastSuccess(string: String) {
        ToastMaker.toastSuccess(activity, string)
    }
}
