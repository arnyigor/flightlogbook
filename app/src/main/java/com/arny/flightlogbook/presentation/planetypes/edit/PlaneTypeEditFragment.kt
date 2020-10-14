package com.arny.flightlogbook.presentation.planetypes.edit


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import com.arny.flightlogbook.R
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.common.FragmentContainerActivity
import com.arny.helpers.utils.KeyboardHelper
import com.arny.helpers.utils.ToastMaker.toastError
import com.arny.helpers.utils.getExtra
import kotlinx.android.synthetic.main.f_plane_type_edit.*
import moxy.ktx.moxyPresenter

class PlaneTypeEditFragment : BaseMvpFragment(), PlaneTypeEditView {
    companion object {
        fun getInstance(bundle: Bundle? = null) = PlaneTypeEditFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private val presenter by moxyPresenter { PlaneTypeEditPresenter() }

    override fun getTitle(): String? = getString(R.string.edit_plane_type)

    override fun getLayoutId(): Int = R.layout.f_plane_type_edit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.planeTypeId = arguments?.getExtra<Long>(CONSTS.EXTRAS.EXTRA_PLANE_TYPE_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSave.setOnClickListener {
            presenter.onSavePlaneType(
                    tiedtPlaneTitle.text.toString(),
                    tiedtRegNo.text.toString(),
                    spinMainType.selectedItemPosition
            )
        }
        tiedtRegNo.doAfterTextChanged { tiLRegNo.error = null }
        tiedtPlaneTitle.doAfterTextChanged { tilPlaneTitle.error = null }
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
        tiedtPlaneTitle.setText(typeName)
    }

    override fun setMainPlaneType(index: Int) {
        spinMainType.setSelection(index)
    }

    override fun setRegNo(regNo: String?) {
        tiedtRegNo.setText(regNo)
    }

    override fun showTitleError(@StringRes strRes: Int) {
        tilPlaneTitle.error = getString(strRes)
    }

    override fun showRegNoError(@StringRes strRes: Int) {
        tiLRegNo.error = getString(strRes)
    }

    override fun setResultOk() {
        val requireActivity = requireActivity()
        if (requireActivity is FragmentContainerActivity) {
            requireActivity.onSuccess(Intent())
            requireActivity.onBackPressed()
        }
    }
}
