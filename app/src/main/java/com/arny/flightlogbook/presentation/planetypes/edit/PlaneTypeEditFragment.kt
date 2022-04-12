package com.arny.flightlogbook.presentation.planetypes.edit

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import com.arny.core.CONSTS
import com.arny.core.utils.KeyboardHelper
import com.arny.core.utils.ToastMaker.toastError
import com.arny.core.utils.getExtra
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.FPlaneTypeEditBinding
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.main.AppRouter
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class PlaneTypeEditFragment : BaseMvpFragment(), PlaneTypeEditView {
    companion object {
        fun getInstance(bundle: Bundle? = null) = PlaneTypeEditFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private lateinit var binding: FPlaneTypeEditBinding
    private var appRouter: AppRouter? = null

    @Inject
    lateinit var presenterProvider: Provider<PlaneTypeEditPresenter>
    private val presenter by moxyPresenter { presenterProvider.get() }

    override fun getTitle(): String = getString(R.string.edit_plane_type)

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
        presenter.planeTypeId = arguments?.getExtra<Long>(CONSTS.EXTRAS.EXTRA_PLANE_TYPE_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FPlaneTypeEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                presenter.onSavePlaneType(
                    binding.tiedtPlaneTitle.text.toString(),
                    binding.tiedtRegNo.text.toString(),
                    binding.spinMainType.selectedItemPosition
                )
            }
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tiedtRegNo.doAfterTextChanged { binding.tiLRegNo.error = null }
        binding.tiedtPlaneTitle.doAfterTextChanged { binding.tilPlaneTitle.error = null }
    }

    override fun onPause() {
        super.onPause()
        KeyboardHelper.hideKeyboard(requireActivity())
    }

    override fun showError(message: String?) {
        toastError(context, message)
    }

    override fun showError(@StringRes strRes: Int) {
        showError(getString(strRes))
    }

    override fun setPlaneTypeName(typeName: String?) {
        binding.tiedtPlaneTitle.setText(typeName)
    }

    override fun setMainPlaneType(index: Int) {
        binding.spinMainType.setSelection(index)
    }

    override fun setRegNo(regNo: String?) {
        binding.tiedtRegNo.setText(regNo)
    }

    override fun showTitleError(@StringRes strRes: Int) {
        binding.tilPlaneTitle.error = getString(strRes)
    }

    override fun showRegNoError(@StringRes strRes: Int) {
        binding.tiLRegNo.error = getString(strRes)
    }

    override fun setResultOk() {
        appRouter?.onReturnResult()
    }
}
