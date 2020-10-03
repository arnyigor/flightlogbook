package com.arny.flightlogbook.presentation.planetypes.list

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.*
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.domain.models.PlaneType
import com.arny.flightlogbook.R
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.presentation.main.NavigateItems
import com.arny.flightlogbook.presentation.main.Router
import com.arny.helpers.utils.ToastMaker
import com.arny.helpers.utils.alertDialog
import com.arny.helpers.utils.inputDialog
import com.arny.helpers.utils.setVisible
import kotlinx.android.synthetic.main.plane_types_layout.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class PlaneTypesFragment : MvpAppCompatFragment(), PlaneTypesView, View.OnClickListener {
    private var adapter: PlaneTypesAdapter? = null

    @InjectPresenter
    lateinit var typeListPresenter: PlaneTypesPresenter

    @ProvidePresenter
    fun provideTypeListPresenter(): PlaneTypesPresenter {
        return PlaneTypesPresenter()
    }

    private var router: Router? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Router) {
            router = context
        }
    }

    companion object {
        fun getInstance(): PlaneTypesFragment {
            return PlaneTypesFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.plane_types_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.str_airplane_types)
        rv_plane_types.layoutManager = LinearLayoutManager(context)
        rv_plane_types.itemAnimator = DefaultItemAnimator()
        adapter = PlaneTypesAdapter(object : PlaneTypesAdapter.PlaneTypesListener {
            override fun onEditType(position: Int, item: PlaneType) {
                router?.navigateTo(
                        NavigateItems.PLANE_TYPE_EDIT,
                        true,
                        bundleOf(CONSTS.EXTRAS.EXTRA_PLANE_TYPE_ID to item.typeId),
                        requestCode = CONSTS.REQUESTS.REQUEST_EDIT_PLANE_TYPE,
                        targetFragment = this@PlaneTypesFragment
                )
            }

            override fun onDeleteType(position: Int, item: PlaneType) {
                showRemoveDialog(item, position)
            }

            override fun onItemClick(position: Int, item: PlaneType) {

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
        tv_no_plane_types.setVisible(vis)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab_add_plane_type -> {
                router?.navigateTo(
                        NavigateItems.PLANE_TYPE_EDIT,
                        true,
                        requestCode = CONSTS.REQUESTS.REQUEST_EDIT_PLANE_TYPE,
                        targetFragment = this@PlaneTypesFragment
                )
            }
        }
    }

    override fun showEditDialog(type: PlaneType, position: Int) {
        inputDialog(
                requireActivity(),
                getString(R.string.str_edt_airplane_types),
                "",
                "",
                type.typeName,
                getString(R.string.str_ok),
                getString(R.string.str_cancel),
                false,
                InputType.TYPE_CLASS_TEXT,
                dialogListener = { result ->
                    typeListPresenter.updatePlaneTypeTitle(type, result, position)
                }
        )
    }

    override fun showRemoveDialog(item: PlaneType, position: Int) {
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