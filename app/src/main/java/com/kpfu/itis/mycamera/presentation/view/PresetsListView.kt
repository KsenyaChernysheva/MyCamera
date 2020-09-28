package com.kpfu.itis.mycamera.presentation.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface PresetsListView : MvpView {

    fun initRecyclerView()

    fun setRecyclerViewItemTouchListener()

    fun backToCamera()

    fun setUpPreset(presetArray: IntArray)
}