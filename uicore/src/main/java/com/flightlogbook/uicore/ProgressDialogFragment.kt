package com.flightlogbook.uicore

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.flightlogbook.uicore.databinding.DialogProgressBinding

fun Fragment.showProgressDialog(progressText: String? = null) {
    var fragment: ProgressDialogFragment? =
        parentFragmentManager.findFragmentByTag(ProgressDialogFragment.TAG) as? ProgressDialogFragment
    if (fragment == null) {
        fragment = ProgressDialogFragment.newInstance(progressText)
        fragment.isCancelable = false
        parentFragmentManager.beginTransaction()
            .add(fragment, ProgressDialogFragment.TAG)
            .commitAllowingStateLoss()
    }
}

fun Fragment.hideProgressDialog() {
    val fragment: ProgressDialogFragment? =
        parentFragmentManager.findFragmentByTag(ProgressDialogFragment.TAG) as? ProgressDialogFragment
    if (fragment != null) {
        parentFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
    }
}

class ProgressDialogFragment : DialogFragment() {
    private lateinit var binding: DialogProgressBinding

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
        binding = DialogProgressBinding.inflate(LayoutInflater.from(requireContext()))
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        val progressText: CharSequence =
            arguments?.getString(PARAM_PROGRESS_TEXT)
                ?: requireContext().getString(R.string.loading)
        binding.tvProgressText.text = progressText
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
