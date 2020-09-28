package com.kpfu.itis.mycamera.presentation.presenter

import android.content.Context
import com.kpfu.itis.mycamera.R
import com.kpfu.itis.mycamera.data.models.Preset
import com.kpfu.itis.mycamera.domain.PreferencesService
import com.kpfu.itis.mycamera.presentation.view.PresetsListView
import moxy.MvpPresenter
import javax.inject.Inject

class PresetsListPresenter @Inject constructor(
    val context: Context,
    val preferencesService: PreferencesService
) : MvpPresenter<PresetsListView>() {

    fun initRecyclerView() {
        viewState.initRecyclerView()
    }

    fun getSavedPresets(): List<Preset> {
        val key = context.getString(R.string.saved_presets_preferences)
        val sharedPref = context.getSharedPreferences(key, Context.MODE_PRIVATE)
        val values =
            sharedPref?.getString(key, "error") ?: "error"
        return preferencesService.parseFromPreference(values)
    }

    fun setPreset(preset: Preset) {
        val presetArray = IntArray(5)
        presetArray[0] = preset.controlMode
        presetArray[1] = preset.whiteBalance
        presetArray[2] = preset.ISO
        presetArray[3] = preset.effect
        presetArray[4] = preset.exposure
        viewState.setUpPreset(presetArray)
    }

    fun updatePresetsPreference(index: Int): List<Preset> {
        val updatedList = getSavedPresets().toMutableList()
        if (updatedList.isNotEmpty()) {
            updatedList.removeAt(index)
        }
        val key = context.getString(R.string.saved_presets_preferences)
        val sharedPref = context.getSharedPreferences(key, Context.MODE_PRIVATE)
        preferencesService.updatePresetsPreference(sharedPref, key, updatedList)
        return updatedList
    }

    fun backToCamera() {
        viewState.backToCamera()
    }

}