package com.arny.flightlogbook.presentation.common

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import moxy.MvpAppCompatFragment

abstract class BaseMvpFragment : MvpAppCompatFragment() {
    protected var title: String? = null
        set(value) {
            field = value
            updateTitle()
        }

    protected open fun isKeyboardHidden(): Boolean = true
    private var wndw: Window? = null
    private var softInputMode: Int = 0

    private fun updateTitle() {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wndw = requireActivity().window
        softInputMode = wndw?.attributes?.softInputMode ?: 0
        if (isKeyboardHidden()) {
            wndw?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        } else {
            wndw?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTitle()
    }

    override fun onDestroy() {
        super.onDestroy()
        wndw?.setSoftInputMode(softInputMode)
    }
}
