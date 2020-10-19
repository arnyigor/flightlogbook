package com.arny.flightlogbook.presentation.planetypes.edit


import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import com.arny.flightlogbook.R
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.main.AppRouter
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

    private var appRouter: AppRouter? = null

    private val presenter by moxyPresenter { PlaneTypeEditPresenter() }

    override fun getTitle(): String? = getString(R.string.edit_plane_type)

    override fun getLayoutId(): Int = R.layout.f_plane_type_edit

    override fun onAttach(context: Context) {
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                presenter.onSavePlaneType(
                        tiedtPlaneTitle.text.toString(),
                        tiedtRegNo.text.toString(),
                        spinMainType.selectedItemPosition
                )
            }
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        appRouter?.onReturnResult()
    }
}
