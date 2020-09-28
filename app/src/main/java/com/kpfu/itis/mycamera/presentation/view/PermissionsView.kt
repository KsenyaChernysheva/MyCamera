package com.kpfu.itis.mycamera.presentation.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface PermissionsView : MvpView {

    fun requestPermissions(code: Int, permissions: Array<String>)

    fun navigateToCamera()
}