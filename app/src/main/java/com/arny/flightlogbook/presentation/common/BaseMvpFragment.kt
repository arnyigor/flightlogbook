package com.arny.flightlogbook.presentation.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.arny.flightlogbook.presentation.main.MainActivity
import moxy.MvpAppCompatFragment

abstract class BaseMvpFragment : MvpAppCompatFragment() {

    @LayoutRes
    protected abstract fun getLayoutId(): Int
    protected open fun getTitle(): String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    protected fun updateTitle() {
        this.activity?.title = getTitle()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTitle()
        val fragmentActivity = activity
        if (fragmentActivity is MainActivity) {
            fragmentActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    fun lockNavDrawer() {
        val fragmentActivity = activity
        if (fragmentActivity is MainActivity) {
            fragmentActivity.lockNavigationDrawer()
        }
    }
}
