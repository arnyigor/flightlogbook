package com.flightlogbook.uicore

import android.app.Dialog
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.arny.core.utils.toPx
import com.arny.flightlogbook.uicore.R

fun FragmentManager.showProgressDialog(progressText: String? = null) {
    var fragment: ProgressDialogFragment? =
        this.findFragmentByTag(ProgressDialogFragment.TAG) as? ProgressDialogFragment
    if (fragment == null) {
        fragment = ProgressDialogFragment.newInstance(progressText)
        fragment.isCancelable = false
        this.beginTransaction()
            .add(fragment, ProgressDialogFragment.TAG)
            .commitAllowingStateLoss()
    }
}

fun FragmentManager.hideProgressDialog() {
    val fragment: ProgressDialogFragment? =
        this.findFragmentByTag(ProgressDialogFragment.TAG) as? ProgressDialogFragment
    if (fragment != null) {
        this.beginTransaction().remove(fragment).commitAllowingStateLoss()
    }
}

class ProgressDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "ProgressDialogFragment"
        private const val PARAM_PROGRESS_TEXT = "PARAM_PROGRESS_TEXT"
        fun newInstance(progressText: String? = null): ProgressDialogFragment {
            return ProgressDialogFragment().apply {
                arguments = bundleOf(
                    PARAM_PROGRESS_TEXT to progressText,
                )
            }
        }
    }

    override fun onCreateDialog(@Nullable savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val parent = LinearLayout(context)
        val pb = ProgressBar(context)
        val tv = TextView(context)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0f)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        val paddings = 16.toPx(context)
        parent.setPadding(paddings)
        parent.orientation = LinearLayout.HORIZONTAL
        tv.setPadding(paddings)
        parent.layoutParams = params
        parent.addView(pb)
        parent.addView(tv)
        builder.setView(parent)
        tv.text = arguments?.getString(PARAM_PROGRESS_TEXT) ?: context.getString(R.string.loading)
        return builder.create().apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog = requireDialog()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}

