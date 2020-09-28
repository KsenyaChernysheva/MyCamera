package com.kpfu.itis.mycamera.presentation.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface CameraView : MvpView {

    fun navigateToPermissionsFragment()

    fun updateCameraSwitchButton(isEnabled: Boolean)

    fun initWhiteBalanceList()

    fun initIsoList()

    fun initExposureList()

    fun initEffectsList()

    fun showParametersList()

    fun hideParametersList()

    fun finishActivity()

}