package com.kpfu.itis.mycamera.presentation.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import com.kpfu.itis.mycamera.R
import com.kpfu.itis.mycamera.di.Injector
import com.kpfu.itis.mycamera.presentation.presenter.AddDialogPresenter
import com.kpfu.itis.mycamera.presentation.view.AddDialogView
import kotlinx.android.synthetic.main.fragment_add_dialog.view.*
import moxy.MvpAppCompatDialogFragment
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class AddDialogFragment() : MvpAppCompatDialogFragment(), AddDialogView {

    private lateinit var dialogView: View

    @Inject
    lateinit var presenterProvider: Provider<AddDialogPresenter>

    private val presenter: AddDialogPresenter by moxyPresenter {
        presenterProvider.get()
    }

    private lateinit var values: String

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.appComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.let {
            values = it.getString(VALUES_KEY) ?: "Error"
        }
        dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_add_dialog, null, false)
        val dialog = AlertDialog.Builder(
            ContextThemeWrapper(
                requireContext(),
                android.R.style.Theme_Material_Dialog
            )
        )
            .setTitle("Add new preset")
            .setPositiveButton("Save") { _, _ ->
                val name = dialogView.et_preset_name.text.toString()
                presenter.savePreset(name, values)
            }
            .setNegativeButton("Cancel") { _, _ ->
                dismiss()
            }
            .setView(dialogView)
            .create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return dialog
    }

    override fun showError() {
        dialogView.tv_error.visibility = View.VISIBLE
    }

    override fun hideError() {
        dialogView.tv_error.visibility = View.INVISIBLE
    }

//    private fun savePreset(name: String, value: String): Boolean {
//        return if (PreferencesService.isNameCorrect(name)) {
//            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
//            var values =
//                sharedPref?.getString(
//                    getString(R.string.saved_presets_preferences),
//                    getString(R.string.pref_default)
//                )
//            val newEntry = PreferencesService.createNewEntry(name, value)
//            values += newEntry
//            sharedPref?.edit()?.apply {
//                putString(getString(R.string.saved_presets_preferences), values)
//                apply()
//            }
//            true
//        } else false
//    }

    companion object {

        const val VALUES_KEY = "values"
        fun show(fragmentManager: FragmentManager, values: String): AddDialogFragment =
            AddDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(VALUES_KEY, values)
                }
                show(fragmentManager, AddDialogFragment::class.java.name)
            }
    }

}