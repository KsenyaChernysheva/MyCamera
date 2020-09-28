package com.kpfu.itis.mycamera.presentation.presenter

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.hardware.camera2.*
import android.os.Handler
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kpfu.itis.mycamera.R
import com.kpfu.itis.mycamera.data.models.CombinedCaptureResult
import com.kpfu.itis.mycamera.data.models.categories.Effect
import com.kpfu.itis.mycamera.data.models.categories.WhiteBalance
import com.kpfu.itis.mycamera.domain.PermissionService
import com.kpfu.itis.mycamera.domain.PreferencesService
import com.kpfu.itis.mycamera.domain.StorageService
import com.kpfu.itis.mycamera.presentation.KEY_EVENT_ACTION
import com.kpfu.itis.mycamera.presentation.MainActivity
import com.kpfu.itis.mycamera.presentation.fragments.CameraFragment
import com.kpfu.itis.mycamera.presentation.view.CameraView
import kotlinx.coroutines.suspendCancellableCoroutine
import moxy.MvpPresenter
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraPresenter @Inject constructor(
    private val storageService: StorageService,
    val context: Context,
    private val permissionService: PermissionService,
    val preferencesService: PreferencesService
) : MvpPresenter<CameraView>() {

    lateinit var camera: CameraDevice
    var currentSettingsId = -1

    val characteristics: CameraCharacteristics by lazy {
        cameraManager.getCameraCharacteristics(getCameraId(lensFacing))
    }

    var lensFacing: Int = CameraCharacteristics.LENS_FACING_BACK

    private val broadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(context)
    }

    val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    val outputDirectory: File by lazy {
        MainActivity.getOutputDirectory(context)
    }

    var currentCameraParams = listOf(
        CaptureRequest.CONTROL_MODE_AUTO,
        CaptureRequest.CONTROL_AWB_MODE_AUTO,
        100,
        CaptureRequest.CONTROL_EFFECT_MODE_OFF,
        0
    ).toIntArray()

    fun registerVolumeDownReceiver(receiver: BroadcastReceiver) {
        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
        broadcastManager.registerReceiver(receiver, filter)
    }

    fun checkPermissions() {
        if (!permissionService.hasPermissions(context)) {
            viewState.navigateToPermissionsFragment()
        }
    }

    fun initPreference() {
        preferencesService.initPreference(context)
    }

    fun getCameraId(lens: Int): String {
        var deviceId = listOf<String>()
        try {
            val cameraIdList = cameraManager.cameraIdList
            deviceId = cameraIdList.filter {
                lens == cameraCharacteristics(it, CameraCharacteristics.LENS_FACING)
            }
        } catch (e: CameraAccessException) {
            Log.e(CameraFragment.TAG, e.toString())
        }
        return deviceId[0]
    }

    fun parametersDetails(imageId: Int) {
        if (imageId != currentSettingsId) {
            when (imageId) {
                paramsImagesIdList[0] -> {
                    viewState.initWhiteBalanceList()
                }
                paramsImagesIdList[1] -> {
                    viewState.initIsoList()
                }
                paramsImagesIdList[2] -> {
                    viewState.initEffectsList()
                }
                else -> {
                    viewState.initExposureList()
                }
            }
            currentSettingsId = imageId
            viewState.showParametersList()
        } else {
            currentSettingsId = -1
            viewState.hideParametersList()
        }
    }

    fun getIsoRange(): IntArray {
        val valuesRange =
            characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        val step =
            ((valuesRange?.upper ?: 10000) - (valuesRange?.lower ?: 100)) / 10
        val values = IntArray(11)
        values[0] = valuesRange?.lower ?: 100
        for (i in 1..10) values[i] = values[i - 1] + step
        return values
    }

    fun getExposureRange(): IntArray {
        val values = IntArray(11)
        for ((index, i) in (-10..10 step 2).withIndex()) {
            values[index] = i
        }
        return values
    }

    @SuppressLint("MissingPermission")
    suspend fun openCamera(
        manager: CameraManager,
        cameraId: String,
        handler: Handler? = null
    ): CameraDevice = suspendCancellableCoroutine { cont ->
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) = cont.resume(device)

            override fun onDisconnected(device: CameraDevice) {
                Log.w(CameraFragment.TAG, "Camera $cameraId has been disconnected")
                viewState.finishActivity()
            }

            override fun onError(device: CameraDevice, error: Int) {
                val msg = when (error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }
                val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                Log.e(CameraFragment.TAG, exc.message, exc)
                if (cont.isActive) cont.resumeWithException(exc)
            }
        }, handler)
    }


    private fun <T> cameraCharacteristics(cameraId: String, key: CameraCharacteristics.Key<T>): T? {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        return when (key) {
            CameraCharacteristics.LENS_FACING -> characteristics.get(key)
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP -> characteristics.get(key)
            else -> throw IllegalArgumentException("Key not recognized")
        }
    }

    fun initLensFacing() {
        lensFacing = getCameraCharacteristics()
    }

    private fun getCameraCharacteristics(): Int {
        return when {
            hasBackCamera() -> CameraCharacteristics.LENS_FACING_BACK
            hasFrontCamera() -> CameraCharacteristics.LENS_FACING_FRONT
            else -> throw IllegalStateException("Back and front camera are unavailable")
        }
    }

    fun hasBackCamera(): Boolean {
        return getCameraId(CameraCharacteristics.LENS_FACING_BACK).isNotEmpty()
    }

    fun hasFrontCamera(): Boolean {
        return getCameraId(CameraCharacteristics.LENS_FACING_FRONT).isNotEmpty()
    }

    suspend fun saveResult(result: CombinedCaptureResult): File =
        storageService.saveResult(result, outputDirectory)

    fun getWhiteBalanceValues(): List<WhiteBalance> = WhiteBalance.values().toList()

    fun getEffectsValues(): List<Effect> = Effect.values().toList()

    companion object {

        val paramsImagesIdList = listOf(
            R.drawable.ic_white_balance,
            R.drawable.ic_iso,
            R.drawable.ic_effect,
            R.drawable.ic_exposure
        )
    }
}