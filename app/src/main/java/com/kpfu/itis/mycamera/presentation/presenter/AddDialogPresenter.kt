package com.kpfu.itis.mycamera.presentation.presenter

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.kpfu.itis.mycamera.R
import com.kpfu.itis.mycamera.domain.PreferencesService
import com.kpfu.itis.mycamera.presentation.view.AddDialogView
import moxy.MvpPresenter
import javax.inject.Inject

class AddDialogPresenter @Inject constructor(
    val context: Context,
    val preferencesService: PreferencesService
) : MvpPresenter<AddDialogView>() {

    fun savePreset(name: String, value: String) {
        if (preferencesService.isNameCorrect(name)) {
            val key = context.getString(R.string.saved_presets_preferences)
            val sharedPref = context.getSharedPreferences(key, MODE_PRIVATE)
            var values =
                sharedPref?.getString(
                    key,
                    context.getString(R.string.pref_default)
                )
            val newEntry = preferencesService.createNewEntry(name, value)
            values += newEntry
            sharedPref?.edit()?.apply {
                putString(key, values)
                apply()
            }
            Log.e("SAVING_PREF", "Successful")
            viewState.hideError()
        } else viewState.showError()
    }
}