package com.kpfu.itis.mycamera.presentation.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface AddDialogView : MvpView {

    fun showError()

    fun hideError()

}