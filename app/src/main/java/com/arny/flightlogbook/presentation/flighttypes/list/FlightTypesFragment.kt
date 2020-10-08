package com.arny.flightlogbook.presentation.flighttypes.list

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.domain.models.FlightType
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.main.AppRouter
import com.arny.helpers.utils.ToastMaker
import com.arny.helpers.utils.alertDialog
import com.arny.helpers.utils.inputDialog
import com.arny.helpers.utils.setVisible
import kotlinx.android.synthetic.main.flight_types_list_layout.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class FlightTypesFragment : MvpAppCompatFragment(), FlightTypesView {
    companion object {
        fun getInstance(): FlightTypesFragment = FlightTypesFragment()
    }

    private var typesAdapter: FlightTypesAdapter? = null

    private var appRouter: AppRouter? = null

    @InjectPresenter
    lateinit var flightTypesPresenter: FlightTypesPresenter

    @ProvidePresenter
    fun provideFlightTypesPresenter(): FlightTypesPresenter {
        return FlightTypesPresenter()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AppRouter) {
            appRouter = context
        }
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(context, msg)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.flight_types_list_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.str_flight_types)
        fab_add_flight_type.setOnClickListener {
            inputDialog(
                    requireActivity(),
                    getString(R.string.enter_type_name),
                    null,
                    null,
                    null,
                    getString(android.R.string.ok),
                    getString(android.R.string.cancel),
                    dialogListener = { result -> flightTypesPresenter.addType(result) }
            )
        }
        typesAdapter = FlightTypesAdapter(object : FlightTypesAdapter.FlightTypesListener {
            override fun onEditType(position: Int, item: FlightType) {
                showEditDialog(item)
            }

            override fun onDeleteType(item: FlightType) {
                showConfirmDeleteDialog(item)
            }
        })
        rv_flight_types.layoutManager = LinearLayoutManager(context)
        rv_flight_types.adapter = typesAdapter
        flightTypesPresenter.loadTypes()
    }

    private fun showConfirmDeleteDialog(item: FlightType) {
        alertDialog(
                requireActivity(),
                getString(R.string.confirm_delete_flight_type),
                null,
                getString(android.R.string.ok),
                getString(android.R.string.cancel),
                true,
                onConfirm = { flightTypesPresenter.removeFlightType(item) }
        )
    }

    private fun showEditDialog(item: FlightType) {
        inputDialog(
                requireActivity(),
                getString(R.string.str_edt_flight_type),
                "",
                "",
                item.typeTitle,
                getString(R.string.str_ok),
                getString(R.string.str_cancel),
                false,
                InputType.TYPE_CLASS_TEXT,
                dialogListener = { result ->
                    flightTypesPresenter.editFlightTypeTitle(item, result)
                }
        )
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
