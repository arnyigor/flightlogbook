package com.arny.flightlogbook.presentation.common

import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import moxy.MvpAppCompatFragment

abstract class BaseMvpFragment : MvpAppCompatFragment() {

    @LayoutRes
    protected abstract fun getLayoutId(): Int
    protected open fun getTitle(): String? = null
    protected open fun isKeyboardHidden(): Boolean = true
    private var wndw: Window? = null
    private var softInputMode: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    protected fun updateTitle() {
        this.activity?.title = getTitle()
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
        val fragmentActivity = activity
        if (fragmentActivity is FragmentContainerActivity) {
            fragmentActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wndw?.setSoftInputMode(softInputMode)
    }
}
