package com.arny.helpers.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class BasePermissions {


    public static final int REQUEST_PERMISSIONS = 111;
    public static final String PERMISSION_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String PERMISSION_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String PERMISSION_READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String PERMISSION_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final String PERMISSION_CALL_PHONE = Manifest.permission.CALL_PHONE;
    public static final String PERMISSION_CALL_PRIVILEGED = Manifest.permission.CALL_PRIVILEGED;

    private String[] permissions = {
            PERMISSION_FINE_LOCATION,
            PERMISSION_COARSE_LOCATION,
            PERMISSION_READ_STORAGE,
            PERMISSION_WRITE_STORAGE,
            PERMISSION_READ_PHONE_STATE,
            PERMISSION_CALL_PHONE,
            PERMISSION_CALL_PRIVILEGED,
    };

    private static final String[] STORAGE_PERMS = {
            PERMISSION_READ_STORAGE,
            PERMISSION_WRITE_STORAGE
    };
    private static final String[] CALL_PERMS = {
            PERMISSION_CALL_PHONE,
            PERMISSION_CALL_PRIVILEGED
    };

    private static final String[] LOCATION_PERMS = {
            PERMISSION_COARSE_LOCATION,
            PERMISSION_FINE_LOCATION
    };

    public static boolean isLocationPermissonGranted(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isBluetoothPermissonGranted(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isReadContactsPermissonGranted(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }


    public static boolean isReadSMSPermissonGranted(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isGetAccountsPermissonGranted(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isReadPhoneStatePermissonGranted(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isStoragePermissonGranted(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isCameraPermissonGranted(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isPhoneCallPermissonGranted(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean canAccessLocation(Activity activity, int requestCode) {
        return (hasPermission(activity, LOCATION_PERMS, PERMISSION_FINE_LOCATION, requestCode));
    }

    public static boolean canAccessStorage(Activity activity, int requestCode) {
        return (hasPermission(activity, STORAGE_PERMS, PERMISSION_READ_STORAGE, requestCode));
    }

    public static boolean canPhoneCall(Activity activity, int requestCode) {
        return (hasPermission(activity, CALL_PERMS, PERMISSION_CALL_PHONE, requestCode));
    }

    public static boolean canReadPhoneState(Activity activity, int requestCode) {
        return (hasPermission(activity, new String[]{PERMISSION_READ_PHONE_STATE}, PERMISSION_READ_PHONE_STATE, requestCode));
    }

    public static boolean permissionGranted(int[] grantResults) {
        boolean granted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        return granted;
    }

    private static boolean hasPermission(Activity activity, String[] permissions, String permision, int requestCode) {
        int permission = ActivityCompat.checkSelfPermission(activity, permision);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    requestCode
            );
        }
        return permission == PackageManager.PERMISSION_GRANTED;
    }
}