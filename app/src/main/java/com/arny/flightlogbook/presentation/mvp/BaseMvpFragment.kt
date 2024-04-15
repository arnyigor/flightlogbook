package com.arny.flightlogbook.presentation.mvp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import moxy.MvpAppCompatFragment

abstract class BaseMvpFragment : MvpAppCompatFragment() {
    protected var title: String? = null
        set(value) {
            field = value
            updateTitle()
        }

    private fun updateTitle() {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTitle()
    }
}
