package com.arny.flightlogbook.presentation.planetypes.list

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.core.CONSTS
import com.arny.core.utils.ToastMaker
import com.arny.core.utils.alertDialog
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.PlaneTypesLayoutBinding
import com.arny.flightlogbook.domain.models.PlaneType
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.main.AppRouter
import com.arny.flightlogbook.presentation.main.NavigateItems
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class PlaneTypesFragment : BaseMvpFragment(), PlaneTypesView, View.OnClickListener {
    companion object {
        fun getInstance(bundle: Bundle? = null) = PlaneTypesFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private lateinit var binding: PlaneTypesLayoutBinding
    private var adapter: PlaneTypesAdapter? = null

    @Inject
    lateinit var presenterProvider: Provider<PlaneTypesPresenter>

    private val presenter by moxyPresenter { presenterProvider.get() }

    private var appRouter: AppRouter? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is AppRouter) {
            appRouter = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun getTitle(): String = getString(R.string.str_airplane_types)

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
        val isRequestField = arguments?.getBoolean(CONSTS.REQUESTS.REQUEST) == true
        binding.rvPlaneTypes.layoutManager = LinearLayoutManager(context)
        binding.rvPlaneTypes.itemAnimator = DefaultItemAnimator()
        adapter = PlaneTypesAdapter(
            false,
            typesListener = object : PlaneTypesAdapter.PlaneTypesListener {
                override fun onEditType(position: Int, item: PlaneType) {
                    appRouter?.navigateTo(
                        NavigateItems.PLANE_TYPE_EDIT,
                        true,
                        bundleOf(CONSTS.EXTRAS.EXTRA_PLANE_TYPE_ID to item.typeId),
                        requestCode = CONSTS.REQUESTS.REQUEST_EDIT_PLANE_TYPE,
                        targetFragment = this@PlaneTypesFragment
                    )
                }

                override fun onDeleteType(position: Int, item: PlaneType) {
                    showRemoveDialog(item)
                }

                override fun onItemClick(position: Int, item: PlaneType) {
                    if (isRequestField) {
                        setFragmentResult(
                            CONSTS.REQUESTS.REQUEST_PLANE_TYPE,
                            bundleOf(CONSTS.EXTRAS.EXTRA_PLANE_TYPE_ID to item.typeId)
                        )
                        requireActivity().onBackPressed()
                    }
                }
            })
        binding.rvPlaneTypes.adapter = adapter
        binding.fabAddPlaneType.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        presenter.loadTypes()
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(context, msg)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun setEmptyViewVisible(vis: Boolean) {
        binding.tvNoPlaneTypes.isVisible = vis
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab_add_plane_type -> {
                appRouter?.navigateTo(
                    NavigateItems.PLANE_TYPE_EDIT,
                    true,
                    requestCode = CONSTS.REQUESTS.REQUEST_EDIT_PLANE_TYPE,
                    targetFragment = this@PlaneTypesFragment
                )
            }
        }
    }

    private fun showRemoveDialog(item: PlaneType) {
        alertDialog(
            context = requireActivity(),
            title = "${getString(R.string.str_delete)} ${item.typeName}?",
            content = null,
            btnOkText = getString(R.string.str_ok),
            btnCancelText = getString(R.string.str_cancel),
            cancelable = false,
            onConfirm = {
                presenter.removeType(item)
            }
        )
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
