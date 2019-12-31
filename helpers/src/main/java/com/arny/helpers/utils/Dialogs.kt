package com.arny.helpers.utils

import android.R
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems

@JvmOverloads
fun ContextThemeWrapper.showAlertDialog(title: String? = null, content: String? = null,
                                        positivePair: Pair<String, (() -> Unit)?>? = null,
                                        negativePair: Pair<String, (() -> Unit)?>? = null,
                                        cancelable: Boolean = true,
                                        style: Int? = null): AlertDialog {
    val builder = if (style != null) AlertDialog.Builder(this, style) else AlertDialog.Builder(this)
    title?.let { builder.setTitle(title) }
    content?.let { builder.setMessage(it) }
    positivePair?.let { builder.setPositiveButton(it.first) { _, _ -> it.second?.invoke() } }
    negativePair?.let { builder.setNegativeButton(it.first) { _, _ -> it.second?.invoke() } }
    builder.setCancelable(cancelable)
    val dialog = builder.create()
    dialog.show()
    return dialog
}

fun Activity.createCustomLayoutDialog(@LayoutRes layout: Int, initView: View.() -> Unit, cancelable: Boolean = true): AlertDialog? {
    val builder = AlertDialog.Builder(this)
    builder.setView(LayoutInflater.from(this).inflate(layout, null, false).apply(initView))
    if (!cancelable) {
        builder.setCancelable(false)
    }
    val dialog = builder.create()
    dialog.show()
    return dialog
}

@JvmOverloads
fun listDialog(context: Context, title: String, items: List<String>, cancelable: Boolean? = false, onSelect: (index: Int, text: String) -> Unit): MaterialDialog? {
    val dlg = MaterialDialog(context)
            .title(text = title)
            .cancelable(cancelable ?: false)
            .listItems(items = items) { _, index, text ->
                onSelect(index, text.toString())
            }
    dlg.show()
    return dlg
}

@JvmOverloads
fun checkDialog(
        context: Context,
        title: String? = null,
        items: Array<String>,
        itemsSelected: Array<Int>? = null,
        cancelable: Boolean = false,
        dialogListener: (index: Int, text: String) -> Unit?): MaterialDialog {
    val dlg = MaterialDialog(context)
            .title(text = title.toString())
            .cancelable(cancelable)
            .listItems(items = items.asList()) { _, index, text ->
                dialogListener(index, text.toString())
            }
            .positiveButton(res = R.string.ok)
    dlg.show()
    return dlg
}

@JvmOverloads
fun alertDialog(
        context: Context?,
        title: String,
        content: String? = null,
        btnOkText: String? = context?.getString(R.string.ok),
        btnCancelText: String? = null,
        cancelable: Boolean = false,
        onConfirm: () -> Unit? = {},
        onCancel: () -> Unit? = {},
        autoDissmiss: Boolean = true
): MaterialDialog? {
    if (!checkContextTheme(context)) return null
    val materialDialog = MaterialDialog(context!!)
    val dialog = materialDialog
    dialog.title(text = title)
    dialog.cancelable(cancelable)
    if (btnOkText != null) {
        dialog.positiveButton(text = btnOkText) {
            if (autoDissmiss) {
                it.dismiss()
            }
            onConfirm.invoke()
        }
    }
    if (btnCancelText != null) {
        dialog.negativeButton(text = btnCancelText) {
            if (autoDissmiss) {
                it.dismiss()
            }
            onCancel.invoke()
        }
    }
    if (!content.isNullOrBlank()) {
        dialog.message(text = fromHtml(content))
    }
    dialog.show()
    return dialog
}

@JvmOverloads
fun inputDialog(
        context: Context,
        title: String,
        content: String? = null,
        hint: String? = null,
        prefill: String? = null,
        btnOkText: String = "OK",
        btnCancelText: String? = "Cancel",
        cancelable: Boolean = true,
        type: Int = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
        dialogListener: (result: String) -> Unit? = {}
): MaterialDialog? {
    if (!checkContextTheme(context)) return null
    val dlg = MaterialDialog(context).show {
        title(text = title)
        if (!content.isNullOrBlank()) {
            message(text = content)
        }
        cancelable(cancelable)
        input(
                hint = hint,
                prefill = prefill,
                inputType = type
        ) { dlg, text ->
            dialogListener(text.toString())
            dlg.dismiss()
        }
        positiveButton(text = btnOkText)
        negativeButton(text = btnCancelText)
    }
    return dlg
}
