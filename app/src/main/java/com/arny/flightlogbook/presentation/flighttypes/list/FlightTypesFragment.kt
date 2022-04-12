package com.arny.flightlogbook.presentation.flighttypes.list

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.core.CONSTS
import com.arny.core.CONSTS.REQUESTS.REQUEST_FLIGHT_TYPE
import com.arny.core.utils.ToastMaker
import com.arny.core.utils.alertDialog
import com.arny.core.utils.inputDialog
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.FlightTypesListLayoutBinding
import com.arny.flightlogbook.domain.models.FlightType
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.main.AppRouter
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class FlightTypesFragment : BaseMvpFragment(), FlightTypesView {
    companion object {
        fun getInstance(bundle: Bundle? = null) = FlightTypesFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private lateinit var binding: FlightTypesListLayoutBinding
    private var typesAdapter: FlightTypesAdapter? = null
    private var appRouter: AppRouter? = null

    @Inject
    lateinit var presenterProvider: Provider<FlightTypesPresenter>
    private val presenter by moxyPresenter { presenterProvider.get() }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is AppRouter) {
            appRouter = context
        }
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(context, msg)
    }

    override fun getTitle(): String = getString(R.string.str_flight_types)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FlightTypesListLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isRequestField = arguments?.getBoolean(CONSTS.REQUESTS.REQUEST) == true
        binding.fabAddFlightType.setOnClickListener {
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
        typesAdapter =
            FlightTypesAdapter(typesListener = object : FlightTypesAdapter.FlightTypesListener {
                override fun onEditType(position: Int, item: FlightType) {
                    showEditDialog(item)
                }

                override fun onDeleteType(item: FlightType) {
                    showConfirmDeleteDialog(item)
                }

                override fun onItemClick(position: Int, item: FlightType) {
                    if (isRequestField) {
                        setFragmentResult(
                            REQUEST_FLIGHT_TYPE,
                            bundleOf(CONSTS.EXTRAS.EXTRA_FLIGHT_TYPE to item.id)
                        )
                        requireActivity().onBackPressed()
                    }
                }
            })
        binding.rvFlightTypes.layoutManager = LinearLayoutManager(context)
        binding.rvFlightTypes.adapter = typesAdapter
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
            context = requireActivity(),
            title = getString(R.string.str_edt_flight_type),
            content = "",
            hint = "",
            prefill = item.typeTitle,
            btnOkText = getString(R.string.str_ok),
            btnCancelText = getString(R.string.str_cancel),
            cancelable = false,
            type = InputType.TYPE_CLASS_TEXT,
            dialogListener = { result ->
                presenter.editFlightTypeTitle(item, result)
            }
        )
    }

    override fun showEmptyView(vis: Boolean) {
        binding.tvFlightTypesEmptyView.isVisible = vis
    }

    override fun updateAdapter(list: List<FlightType>) {
        typesAdapter?.addAll(list)
    }
}
