package com.kpfu.itis.mycamera.presentation.presenter

import android.content.Context
import com.kpfu.itis.mycamera.domain.PermissionService
import com.kpfu.itis.mycamera.domain.PermissionService.Companion.PERMISSIONS_REQUEST_CODE
import com.kpfu.itis.mycamera.domain.PermissionService.Companion.PERMISSIONS_REQUIRED
import com.kpfu.itis.mycamera.presentation.view.PermissionsView
import moxy.MvpPresenter
import javax.inject.Inject

class PermissionsPresenter @Inject constructor(
    val context: Context,
    private val permissionService: PermissionService
) : MvpPresenter<PermissionsView>() {

    fun checkPermissions() {
        if (!permissionService.hasPermissions(context)) {
            viewState.requestPermissions(PERMISSIONS_REQUEST_CODE, PERMISSIONS_REQUIRED)
        } else {
            viewState.navigateToCamera()
        }
    }
}