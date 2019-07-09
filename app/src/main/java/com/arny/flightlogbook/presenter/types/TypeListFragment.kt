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
import com.arny.flightlogbook.adapter.FlightTypesAdapter
import com.arny.flightlogbook.data.interfaces.FragmentResultListener
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.utils.ToastMaker
import com.arny.flightlogbook.utils.Utility
import com.arny.flightlogbook.utils.dialogs.ConfirmDialogListener
import com.arny.flightlogbook.utils.dialogs.InputDialogListener
import com.arny.flightlogbook.utils.dialogs.confirmDialog
import com.arny.flightlogbook.utils.dialogs.inputDialog
import com.arny.flightlogbook.utils.empty
import com.arny.flightlogbook.utils.setVisible
import kotlinx.android.synthetic.main.fragment_type_list.*

class TypeListFragment : MvpAppCompatFragment(), TypeListView, View.OnClickListener {
    private var adapter: FlightTypesAdapter? = null
    private var fragmentResultListener: FragmentResultListener? = null

    @InjectPresenter
    lateinit var typeListPresenter: TypeListPresenterImpl

    @ProvidePresenter
    fun provideTypeListPresenter(): TypeListPresenterImpl {
        return TypeListPresenterImpl()
    }

    companion object {
        fun getInstance(): TypeListFragment {
            return TypeListFragment()
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_type_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        typelistView.layoutManager = LinearLayoutManager(context)
        typelistView.itemAnimator = DefaultItemAnimator()
        adapter = FlightTypesAdapter(object : FlightTypesAdapter.FlightTypesListener {
            override fun onTypeEdit(position: Int, item: AircraftType) {
                typeListPresenter.confirmEditType(item)
            }

            override fun onTypeDelete(position: Int, item: AircraftType) {
                typeListPresenter.confirmDeleteType(item)
            }

            override fun onItemClick(position: Int, item: AircraftType) {

            }
        })
        typelistView.adapter = adapter
        removeallTypes.setOnClickListener(this)
        addType.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.removeallTypes -> {
            }
            R.id.addType -> {
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

    override fun showEditDialog(type: AircraftType) {
        context?.let { ctx ->
            inputDialog(ctx, getString(R.string.str_edt_airplane_types), "", type.typeName, getString(R.string.str_ok), getString(R.string.str_cancel), false, InputType.TYPE_CLASS_TEXT, object : InputDialogListener {
                override fun onConfirm(newName: String) {
                    if (!Utility.empty(newName)) {
                        type.typeName = newName
                        typeListPresenter.updateType(type)
                    } else {
                        Toast.makeText(ctx, R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancel() {

                }
            })
        }
    }

    override fun showRemoveDialog(item: AircraftType) {
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

    override fun updateAdapter(list: ArrayList<AircraftType>) {
        adapter?.addAll(list)
        removeallTypes.setVisible(!adapter?.getItems().isNullOrEmpty())
    }

    override fun toastSuccess(string: String) {
        ToastMaker.toastSuccess(activity, string)
    }
}
