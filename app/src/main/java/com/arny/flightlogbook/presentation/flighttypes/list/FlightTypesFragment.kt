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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arny.core.CONSTS
import com.arny.core.CONSTS.REQUESTS.REQUEST_FLIGHT_TYPE
import com.arny.core.utils.ToastMaker
import com.arny.core.utils.alertDialog
import com.arny.core.utils.inputDialog
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.FlightTypesListLayoutBinding
import com.arny.flightlogbook.domain.models.FlightType
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.navigation.OpenDrawerListener
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class FlightTypesFragment : BaseMvpFragment(), FlightTypesView {
    private lateinit var binding: FlightTypesListLayoutBinding
    private val args: FlightTypesFragmentArgs by navArgs()
    private var typesAdapter: FlightTypesAdapter? = null
    private var openDrawerListener: OpenDrawerListener? = null

    @Inject
    lateinit var presenterProvider: Provider<FlightTypesPresenter>
    private val presenter by moxyPresenter { presenterProvider.get() }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is OpenDrawerListener) {
            openDrawerListener = context
        }
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(context, msg)
    }

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
        val isRequestField = args.isRequestField
        title = if (isRequestField) {
            getString(R.string.choose_flight_types)
        } else {
            getString(R.string.str_flight_types)
        }
        openDrawerListener?.onChangeHomeButton(isRequestField)
        binding.fabAddFlightType.setOnClickListener {
            inputDialog(
                context = requireActivity(),
                title = getString(R.string.enter_type_name),
                btnOkText = getString(android.R.string.ok),
                btnCancelText = getString(android.R.string.cancel),
                dialogListener = { result -> presenter.addType(result) }
            )
        }
        typesAdapter = FlightTypesAdapter(
            hideEdit = isRequestField,
            onEditType = (::showEditDialog),
            onDeleteType = (::showConfirmDeleteDialog),
            onItemClick = { item ->
                onItemSelected(isRequestField, item)
            }
        )
        binding.rvFlightTypes.adapter = typesAdapter
        presenter.loadTypes()
    }

    private fun onItemSelected(isRequestField: Boolean, item: FlightType) {
        if (isRequestField) {
            setFragmentResult(
                REQUEST_FLIGHT_TYPE,
                bundleOf(CONSTS.EXTRAS.EXTRA_FLIGHT_TYPE to item.id)
            )
            findNavController().popBackStack()
        }
    }

    private fun showConfirmDeleteDialog(item: FlightType) {
        alertDialog(
            context = requireActivity(),
            title = getString(R.string.confirm_delete_flight_type),
            content = null,
            btnOkText = getString(android.R.string.ok),
            btnCancelText = getString(android.R.string.cancel),
            cancelable = true,
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
        typesAdapter?.submitList(list)
    }
}
