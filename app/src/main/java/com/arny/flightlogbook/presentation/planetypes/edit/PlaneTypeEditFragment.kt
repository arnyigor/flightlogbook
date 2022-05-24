package com.arny.flightlogbook.presentation.planetypes.edit

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arny.core.CONSTS
import com.arny.core.utils.KeyboardHelper
import com.arny.core.utils.ToastMaker.toastError
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.FPlaneTypeEditBinding
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class PlaneTypeEditFragment : BaseMvpFragment(), PlaneTypeEditView {
    private val args: PlaneTypeEditFragmentArgs by navArgs()
    private lateinit var binding: FPlaneTypeEditBinding

    @Inject
    lateinit var presenterProvider: Provider<PlaneTypeEditPresenter>
    private val presenter by moxyPresenter { presenterProvider.get() }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        presenter.planeTypeId = args.planeTypeId
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            R.id.action_save -> {
                presenter.onSavePlaneType(
                    binding.tiedtPlaneTitle.text.toString(),
                    binding.tiedtRegNo.text.toString(),
                    binding.spinMainType.selectedItemPosition
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = getString(R.string.edit_plane_type)
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
        setFragmentResult(
            CONSTS.REQUESTS.REQUEST_PLANE_TYPE_EDIT,
            bundleOf()
        )
        findNavController().popBackStack()
    }
}
