package com.arny.flightlogbook.presentation.flighttypes

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.arny.domain.models.FlightType
import com.arny.flightlogbook.R
import com.arny.helpers.utils.*
import kotlinx.android.synthetic.main.flight_types_list_layout.*

class FlightTypesFragment : MvpAppCompatFragment(), FlightTypesView {
    private var typesAdapter: FlightTypesAdapter? = null

    @InjectPresenter
    lateinit var flightTypesPresenter: FlightTypesPresenter

    @ProvidePresenter
    fun provideFlightTypesPresenter(): FlightTypesPresenter {
        return FlightTypesPresenter()
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(context, msg)
    }

    companion object {
        @JvmStatic
        fun getInstance(): FlightTypesFragment {
            return FlightTypesFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.flight_types_list_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab_add_flight_type.setOnClickListener {
            context?.let { ctx ->
                inputDialog(ctx, "Введите название типа", null,null, getString(android.R.string.ok), getString(android.R.string.cancel),dialogListener = object : InputDialogListener {
                    override fun onConfirm(content: String?) {
                        flightTypesPresenter.addType(content)
                    }

                    override fun onCancel() {
                    }
                })
            }
        }
        typesAdapter = FlightTypesAdapter(object : FlightTypesAdapter.FlightTypesListener {
            override fun onEditType(position: Int, item: FlightType) {
                showEditDialog(item)
            }

            override fun onDeleteType(item: FlightType) {
                showConfirmDeleteDialog(item)
            }

            override fun onItemClick(position: Int, item: FlightType) {

            }

        })
        rv_flight_types.layoutManager = LinearLayoutManager(context)
        rv_flight_types.adapter = typesAdapter
        flightTypesPresenter.loadTypes()
    }

    private fun showConfirmDeleteDialog(item: FlightType) {
        context?.let { ctx->
            confirmDialog(ctx, getString(R.string.confirm_delete_flight_type), null, getString(android.R.string.ok), getString(android.R.string.cancel), true, object : ConfirmDialogListener {
                override fun onConfirm() {
                    flightTypesPresenter.removeFlightType(item)
                }

                override fun onCancel() {

                }
            })
        }
    }


    private fun showEditDialog(item: FlightType) {
        context?.let {ctx->
            inputDialog(ctx, getString(R.string.str_edt_flight_type), "", item.typeTitle, getString(R.string.str_ok), getString(R.string.str_cancel), false, InputType.TYPE_CLASS_TEXT, object : InputDialogListener {
                override fun onConfirm(newName: String) {
                    flightTypesPresenter.editFlightTypeTitle(item,newName)
                }

                override fun onCancel() {

                }
            })
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun showEmptyView(vis: Boolean) {
        tv_flight_types_empty_view.setVisible(vis)
    }

    override fun updateAdapter(list: List<FlightType>) {
        typesAdapter?.addAll(list)
    }
}
