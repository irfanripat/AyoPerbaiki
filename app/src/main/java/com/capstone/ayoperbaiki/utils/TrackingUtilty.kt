package com.capstone.ayoperbaiki.utils

import android.Manifest
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions

object TrackingUtility {

    fun hasReadExternalStoragePermission(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

    fun hasCameraPermission(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.CAMERA
        )

}