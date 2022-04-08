package com.arny.core.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.DisplayMetrics
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlin.math.roundToInt

fun <T> Fragment.requestPermission(
    resultLauncher: ActivityResultLauncher<T>,
    permission: String,
    input: T,
    checkPermissionOk: (input: T) -> Unit = {}
) {
    when {
        ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED -> {
            checkPermissionOk(input)
        }
        shouldShowRequestPermissionRationale(permission) -> {
            resultLauncher.launch(input)
        }
        else -> {
            resultLauncher.launch(input)
        }
    }
}

fun Int.toPx(context: Context): Int =
    (this * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()

fun <T> Activity.requestPermission(
    resultLauncher: ActivityResultLauncher<T>,
    permission: String,
    input: T,
    checkPermissionOk: () -> Unit = {}
) {
    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(this, permission) -> {
            checkPermissionOk()
        }
        else -> {
            resultLauncher.launch(input)
        }
    }
}