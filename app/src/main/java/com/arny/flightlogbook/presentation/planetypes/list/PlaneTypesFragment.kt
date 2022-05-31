package com.arny.flightlogbook.presentation.planetypes.list

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.core.CONSTS
import com.arny.core.utils.ToastMaker
import com.arny.core.utils.alertDialog
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.PlaneTypesLayoutBinding
import com.arny.flightlogbook.domain.models.PlaneType
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.navigation.OpenDrawerListener
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import java.util.*
import javax.inject.Inject
import javax.inject.Provider

class PlaneTypesFragment : BaseMvpFragment(), PlaneTypesView {
    private val args: PlaneTypesFragmentArgs by navArgs()
    private lateinit var binding: PlaneTypesLayoutBinding
    private var adapter: PlaneTypesAdapter? = null
    private var openDrawerListener: OpenDrawerListener? = null

    @Inject
    lateinit var presenterProvider: Provider<PlaneTypesPresenter>
    private val presenter by moxyPresenter { presenterProvider.get() }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is OpenDrawerListener) {
            openDrawerListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PlaneTypesLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isRequestField = args.isRequestField
        openDrawerListener?.onChangeHomeButton(isRequestField)
        title = if (isRequestField) {
            getString(R.string.choose_airplne_type)
        } else {
            getString(R.string.str_airplane_types)
        }
        initUi(isRequestField)
        setFragmentResultListener(CONSTS.REQUESTS.REQUEST_PLANE_TYPE_EDIT) { _, _ ->
            presenter.loadTypes()
        }
    }

    private fun initUi(isRequestField: Boolean) {
        with(binding) {
            rvPlaneTypes.layoutManager = LinearLayoutManager(context)
            rvPlaneTypes.itemAnimator = DefaultItemAnimator()
            adapter = PlaneTypesAdapter(
                hideEdit = isRequestField,
                onEditType = (::navigateTypeEdit),
                onDeleteType = (::showRemoveDialog),
                onItemClick = { item ->
                    onItemClicked(isRequestField, item)
                }
            )
            rvPlaneTypes.adapter = adapter
            fabAddPlaneType.setOnClickListener { addPlaneType() }
        }
    }

    private fun addPlaneType() {
        requireView().findNavController().navigate(
            PlaneTypesFragmentDirections.actionNavPlaneTypesToPlaneTypeEditFragment()
        )
    }

    private fun onItemClicked(
        isRequestField: Boolean,
        item: PlaneType
    ) {
        if (isRequestField) {
            setFragmentResult(
                CONSTS.REQUESTS.REQUEST_PLANE_TYPE,
                bundleOf(CONSTS.EXTRAS.EXTRA_PLANE_TYPE_ID to item.typeId)
            )
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.loadTypes()
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(context, msg)
    }

    private fun navigateTypeEdit(item: PlaneType) {
        val typeId = item.typeId
        typeId?.let {
            findNavController().navigate(
                PlaneTypesFragmentDirections.actionNavPlaneTypesToPlaneTypeEditFragment(typeId)
            )
        }
    }

    override fun setEmptyViewVisible(vis: Boolean) {
        binding.tvNoPlaneTypes.isVisible = vis
    }

    private fun showRemoveDialog(item: PlaneType) {
        alertDialog(
            context = requireActivity(),
            title = "${getString(R.string.str_delete)}${getPlaneName(item)}?",
            content = null,
            btnOkText = getString(R.string.str_ok),
            btnCancelText = getString(R.string.str_cancel),
            cancelable = false,
            onConfirm = {
                presenter.removeType(item)
            }
        )
    }

    private fun getPlaneName(item: PlaneType): String {
        val hasName = !item.typeName.isNullOrBlank()
        val hasRegNo = !item.regNo.isNullOrBlank()
        val mainType = item.mainType?.nameRes?.let { "${getString(it)}\u00A0" }.orEmpty()
        val regNo = getString(R.string.str_regnum_formatted, item.regNo)
        return when {
            hasName && hasRegNo -> {
                String.format(
                    Locale.getDefault(),
                    " %s%s\u00A0%s",
                    mainType,
                    item.typeName,
                    regNo,
                )
            }
            !hasName && hasRegNo -> {
                String.format(
                    Locale.getDefault(),
                    " %s%s",
                    mainType,
                    regNo,
                )
            }
            else -> mainType
        }
    }

    override fun notifyItemChanged(position: Int) {
        adapter?.notifyItemChanged(position)
    }

    override fun updateAdapter(list: List<PlaneType>) {
        adapter?.submitList(list.toMutableList())
    }

    override fun toastSuccess(string: String) {
        ToastMaker.toastSuccess(activity, string)
    }
}
