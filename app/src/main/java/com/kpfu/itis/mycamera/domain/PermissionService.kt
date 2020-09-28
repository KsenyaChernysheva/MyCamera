package com.kpfu.itis.mycamera.domain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionService {

    fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val PERMISSIONS_REQUEST_CODE = 10
        val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
    }
}