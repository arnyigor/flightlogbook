package com.arny.flightlogbook.presentation.flighttypes.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.core.CONSTS
import com.arny.core.utils.ToastMaker
import com.arny.core.utils.alertDialog
import com.arny.core.utils.inputDialog
import com.arny.domain.models.FlightType
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.main.AppRouter
import kotlinx.android.synthetic.main.flight_types_list_layout.*
import moxy.ktx.moxyPresenter

class FlightTypesFragment : BaseMvpFragment(), FlightTypesView {
    companion object {
        fun getInstance(bundle: Bundle? = null) = FlightTypesFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private var typesAdapter: FlightTypesAdapter? = null
    private var appRouter: AppRouter? = null

    private val presenter by moxyPresenter { FlightTypesPresenter() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AppRouter) {
            appRouter = context
        }
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(context, msg)
    }

    override fun getLayoutId(): Int = R.layout.flight_types_list_layout

    override fun getTitle(): String? = getString(R.string.str_flight_types)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isRequestField = arguments?.getBoolean(CONSTS.REQUESTS.REQUEST) == true
        fab_add_flight_type.setOnClickListener {
            inputDialog(
                    requireActivity(),
                    getString(R.string.enter_type_name),
                    null,
                    null,
                    null,
                    getString(android.R.string.ok),
                    getString(android.R.string.cancel),
                    dialogListener = { result -> presenter.addType(result) }
            )
        }
        typesAdapter = FlightTypesAdapter(typesListener = object : FlightTypesAdapter.FlightTypesListener {
            override fun onEditType(position: Int, item: FlightType) {
                showEditDialog(item)
            }

            override fun onDeleteType(item: FlightType) {
                showConfirmDeleteDialog(item)
            }

            override fun onItemClick(position: Int, item: FlightType) {
                if (isRequestField) {
                    appRouter?.setResultToTargetFragment(
                            this@FlightTypesFragment,
                            Intent().apply {
                                putExtra(CONSTS.EXTRAS.EXTRA_FLIGHT_TYPE, item.id)
                            })
                }
            }
        })
        rv_flight_types.layoutManager = LinearLayoutManager(context)
        rv_flight_types.adapter = typesAdapter
        presenter.loadTypes()
    }

    private fun showConfirmDeleteDialog(item: FlightType) {
        alertDialog(
                requireActivity(),
                getString(R.string.confirm_delete_flight_type),
                null,
                getString(android.R.string.ok),
                getString(android.R.string.cancel),
                true,
                onConfirm = { presenter.removeFlightType(item) }
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
                    presenter.editFlightTypeTitle(item, result)
                }
        )
    }

    override fun showEmptyView(vis: Boolean) {
        tv_flight_types_empty_view.isVisible = vis
    }

    override fun updateAdapter(list: List<FlightType>) {
        typesAdapter?.addAll(list)
    }
}
