package com.example.cmsfp.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

public class Permissions {

    public static boolean checkCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkGalleryPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

}
