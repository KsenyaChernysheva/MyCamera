package com.kpfu.itis.mycamera.presentation.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.Navigation
import com.kpfu.itis.mycamera.R
import com.kpfu.itis.mycamera.di.Injector
import com.kpfu.itis.mycamera.domain.PermissionService.Companion.PERMISSIONS_REQUEST_CODE
import com.kpfu.itis.mycamera.presentation.presenter.PermissionsPresenter
import com.kpfu.itis.mycamera.presentation.view.PermissionsView
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class PermissionsFragment : MvpAppCompatFragment(), PermissionsView {

    @Inject
    lateinit var presenterProvider: Provider<PermissionsPresenter>

    private val presenter: PermissionsPresenter by moxyPresenter {
        presenterProvider.get()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        presenter.checkPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                navigateToCamera()
            } else {
                Toast.makeText(context, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun requestPermissions(code: Int, permissions: Array<String>) {
        requestPermissions(permissions, code)
    }

    override fun navigateToCamera() {
        Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
            PermissionsFragmentDirections.actionPermissionsToCamera()
        )
    }
}
