package com.arny.core.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object KeyboardHelper {
    @JvmOverloads
    fun hideKeyboard(activity: Activity?, flags: Int = 0) {
        try {
            if (activity != null) {
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                val focus = activity.window.decorView.rootView
                if (focus != null) {
                    imm?.hideSoftInputFromWindow(focus.windowToken, flags)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmOverloads
    fun hideKeyboard(context: Context, view: View, flags: Int = 0) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken, flags)
    }
}