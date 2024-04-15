package com.arny.flightlogbook.presentation.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import android.util.DisplayMetrics
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlin.math.roundToInt
import android.provider.Settings

fun Fragment.requestPermission(
    resultLauncher: ActivityResultLauncher<String>,
    permission: String,
    permissionOk: (permission: String) -> Unit = {}
) {
    if (ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        permissionOk(permission)
    } else {
        resultLauncher.launch(permission)
    }
}

fun Fragment.permissionRationale(permission: String) =
    !shouldShowRequestPermissionRationale(permission)

fun Context.goToAppInfo() {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    val uri: Uri = Uri.fromParts("package", this.packageName, null)
    intent.data = uri
    intent.flags = FLAG_ACTIVITY_NEW_TASK
    this.startActivity(intent)
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