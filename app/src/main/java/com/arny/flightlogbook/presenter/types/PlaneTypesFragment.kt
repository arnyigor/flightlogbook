package com.arny.flightlogbook.presenter.types

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.view.*
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.interfaces.FragmentResultListener
import com.arny.flightlogbook.data.models.PlaneType
import com.arny.flightlogbook.utils.ToastMaker
import com.arny.flightlogbook.utils.dialogs.ConfirmDialogListener
import com.arny.flightlogbook.utils.dialogs.InputDialogListener
import com.arny.flightlogbook.utils.dialogs.confirmDialog
import com.arny.flightlogbook.utils.dialogs.inputDialog
import com.arny.flightlogbook.utils.empty
import com.arny.flightlogbook.utils.setVisible
import kotlinx.android.synthetic.main.types_layout.*

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

    override fun toastError(msg: String?) {
        ToastMaker.toastError(context, msg)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val parent = activity
        if (parent is FragmentResultListener) {
            fragmentResultListener = parent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    override fun setAdapterVisible(vis: Boolean) {

    }

    override fun setEmptyViewVisible(vis: Boolean) {

    }

    override fun itemRemoved(position: Int) {

    }

    override fun clearAdapter() {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.types_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_plane_types.layoutManager = LinearLayoutManager(context)
        rv_plane_types.itemAnimator = DefaultItemAnimator()
        adapter = PlaneTypesAdapter(object : PlaneTypesAdapter.FlightTypesListener {
            override fun onTypeEdit(position: Int, item: PlaneType) {
                showEditDialog(item, position)
            }

            override fun onTypeDelete(position: Int, item: PlaneType) {
                showRemoveDialog(item, position)
            }

            override fun onItemClick(position: Int, item: PlaneType) {

            }
        })
        rv_plane_types.adapter = adapter
        btn_remove_all_plane_types.setOnClickListener(this)
        btn_add_plane_type.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_remove_all_plane_types -> {
                context?.let { ctx ->
                    confirmDialog(ctx, getString(R.string.str_remove_all) + "?", null, getString(R.string.str_ok), getString(R.string.str_cancel), true, object : ConfirmDialogListener {
                        override fun onConfirm() {
                            typeListPresenter.removeAllPlaneTypes()
                        }

                        override fun onCancel() {

                        }

                    })
                }
            }
            R.id.btn_add_plane_type -> {
                context?.let { ctx ->
                    inputDialog(ctx, getString(R.string.str_add_airplane_types), "", "", getString(R.string.str_ok), getString(R.string.str_cancel), false, InputType.TYPE_CLASS_TEXT, object : InputDialogListener {
                        override fun onConfirm(name: String) {
                            if (!name.empty()) {
                                typeListPresenter.addType(name)
                            } else {
                                Toast.makeText(ctx, R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onCancel() {

                        }

                    })
                }

            }
        }
    }

    override fun showEditDialog(type: PlaneType, position: Int) {
        context?.let { ctx ->
            inputDialog(ctx, getString(R.string.str_edt_airplane_types), "", type.typeName, getString(R.string.str_ok), getString(R.string.str_cancel), false, InputType.TYPE_CLASS_TEXT, object : InputDialogListener {
                override fun onConfirm(newName: String) {
                    typeListPresenter.updatePlaneTypeTitle(type, newName, position)
                }

                override fun onCancel() {

                }
            })
        }
    }

    override fun showRemoveDialog(item: PlaneType, position: Int) {
        context?.let { ctx ->
            confirmDialog(ctx, "Вы хотите удалить ${item.typeName}", null, "Да", "Нет", false, object : ConfirmDialogListener {
                override fun onConfirm() {
                    typeListPresenter.removeType(item)
                }

                override fun onCancel() {

                }
            })
        }
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

    override fun setBtnRemoveAllVisible(vis: Boolean) {
        btn_remove_all_plane_types.setVisible(vis)
    }
}
