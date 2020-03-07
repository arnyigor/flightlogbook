package com.arny.flightlogbook.presentation.planetypes

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.domain.models.PlaneType
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.interfaces.FragmentResultListener
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
    private var fragmentResultListener: FragmentResultListener? = null
    @InjectPresenter
    lateinit var typeListPresenter: PlaneTypesPresenter

    @ProvidePresenter
    fun provideTypeListPresenter(): PlaneTypesPresenter {
        return PlaneTypesPresenter()
    }

    companion object {
        fun getInstance(): PlaneTypesFragment {
            return PlaneTypesFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parent = activity
        if (parent is FragmentResultListener) {
            fragmentResultListener = parent
        }
    }

    override fun onDetach() {
        super.onDetach()
        fragmentResultListener = null
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
        rv_plane_types.layoutManager = LinearLayoutManager(context)
        rv_plane_types.itemAnimator = DefaultItemAnimator()
        adapter = PlaneTypesAdapter(object : PlaneTypesAdapter.PlaneTypesListener {
            override fun onEditType(position: Int, item: PlaneType) {
                showEditDialog(item, position)
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
                inputDialog(
                        requireActivity(),
                        getString(R.string.str_add_airplane_types),
                        "",
                        "",
                        "",
                        getString(R.string.str_ok),
                        getString(R.string.str_cancel),
                        false,
                        InputType.TYPE_CLASS_TEXT,
                        dialogListener = { result ->
                            if (result.isNotBlank()) {
                                typeListPresenter.addType(result)
                            } else {
                                Toast.makeText(requireActivity(), R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show()
                            }
                        }
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
                "Вы хотите удалить ${item.typeName}?",
                null,
                "Да",
                "Нет",
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
