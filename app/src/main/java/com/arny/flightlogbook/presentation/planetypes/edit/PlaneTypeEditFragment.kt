package com.arny.flightlogbook.presentation.planetypes.edit


import android.os.Bundle
import android.view.View
import com.arny.flightlogbook.R
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.main.BackButtonListener
import com.arny.helpers.utils.ToastMaker.toastError
import com.arny.helpers.utils.getExtra
import kotlinx.android.synthetic.main.f_plane_type_edit.*
import moxy.ktx.moxyPresenter

class PlaneTypeEditFragment : BaseMvpFragment(), PlaneTypeEditView, BackButtonListener {
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
    }

    override fun showError(message: String?) {
        toastError(context, message)
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

    override fun onBackPressed(): Boolean = true
}
