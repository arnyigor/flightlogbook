package com.arny.flightlogbook.customfields.presentation.edit


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.arny.flightlogbook.customfields.R
import moxy.MvpAppCompatFragment

class CustomFieldEditFragment : MvpAppCompatFragment() {

    companion object {
        private const val PARAM_FIELD_ID = "PARAM_FIELD_ID"

        @JvmStatic
        fun getInstance(id: Long?) = CustomFieldEditFragment().apply {
            arguments = bundleOf(PARAM_FIELD_ID to id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_custom_field_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
