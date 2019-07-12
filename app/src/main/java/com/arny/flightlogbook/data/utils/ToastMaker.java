package com.arny.flightlogbook.data.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class ToastMaker {

	public static void toast(Context context, String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
	}

	public static void toastError(final Context context, final String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toasty.error(context, message, Toast.LENGTH_LONG).show());
	}

	public static void toastInfo(Context context, String message) {
		Toasty.info(context, message, Toast.LENGTH_LONG).show();
	}

	public static void toastSuccess(final Context context, final String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toasty.success(context, message, Toast.LENGTH_LONG).show());
	}

}