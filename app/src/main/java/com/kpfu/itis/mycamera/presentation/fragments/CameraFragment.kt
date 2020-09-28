package com.kpfu.itis.mycamera.presentation.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import androidx.camera.core.CameraInfoUnavailableException
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kpfu.itis.mycamera.R
import com.kpfu.itis.mycamera.data.models.CombinedCaptureResult
import com.kpfu.itis.mycamera.di.Injector
import com.kpfu.itis.mycamera.presentation.KEY_EVENT_EXTRA
import com.kpfu.itis.mycamera.presentation.lists.EffectsAdapter
import com.kpfu.itis.mycamera.presentation.lists.ExposureAdapter
import com.kpfu.itis.mycamera.presentation.lists.ParametersAdapter
import com.kpfu.itis.mycamera.presentation.lists.WhiteBalanceAdapter
import com.kpfu.itis.mycamera.presentation.presenter.CameraPresenter
import com.kpfu.itis.mycamera.presentation.presenter.CameraPresenter.Companion.paramsImagesIdList
import com.kpfu.itis.mycamera.presentation.view.CameraView
import com.kpfu.itis.mycamera.utils.*
import kotlinx.android.synthetic.main.camera_ui_container.*
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Provider
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CameraFragment : MvpAppCompatFragment(), CameraView {

    @Inject
    lateinit var presenterProvider: Provider<CameraPresenter>

    private val presenter: CameraPresenter by moxyPresenter {
        presenterProvider.get()
    }

    private lateinit var viewFinder: AutoFitSurfaceView
    private lateinit var overlay: View
    private lateinit var relativeOrientation: OrientationLiveData

    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)

    private lateinit var imageReader: ImageReader
    private lateinit var session: CameraCaptureSession
    private val imageReaderThread = HandlerThread("imageReaderThread").apply { start() }
    private val imageReaderHandler = Handler(imageReaderThread.looper)

    private val animationTask: Runnable by lazy {
        Runnable {
            overlay.elevation = 10F
            overlay.background = Color.argb(150, 255, 255, 255).toDrawable()
            overlay.postDelayed({
                overlay.elevation = 0F
                overlay.background = Color.BLACK.toDrawable()
            }, ANIMATION_FAST_MILLIS)
        }
    }

    private val volumeDownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    val shutter = camera_capture_button
                    shutter.simulateClick()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        if (arguments?.isEmpty != true) {
            arguments?.let {
                presenter.currentCameraParams = it.getIntArray(PRESET_ARGUMENT)!!
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.checkPermissions()
    }

    override fun onStop() {
        super.onStop()
        try {
            presenter.camera.close()
        } catch (exc: Throwable) {
            Log.e(TAG, "Error closing camera", exc)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraThread.quitSafely()
        imageReaderThread.quitSafely()
    }

    override fun finishActivity() {
        requireActivity().finish()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera, container, false)

    private fun setGalleryThumbnail(uri: Uri) {
        val thumbnail = photo_view_button
        thumbnail.post {
            thumbnail.setPadding(resources.getDimension(R.dimen.stroke_small).toInt())
            Glide.with(thumbnail)
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbnail)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewFinder = view_finder
        overlay = view.findViewById(R.id.overlay)
        viewFinder.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) = Unit

            override fun surfaceCreated(holder: SurfaceHolder) {
                val previewSize = getPreviewOutputSize(
                    viewFinder.display, presenter.characteristics, SurfaceHolder::class.java
                )
                Log.d(TAG, "View finder size: ${viewFinder.width} x ${viewFinder.height}")
                Log.d(TAG, "Selected preview size: $previewSize")
                viewFinder.setAspectRatio(previewSize.width, previewSize.height)
                view.post {
                    initializeCamera()
                    presenter.initLensFacing()
                    updateCameraUi()
                    updateCameraSwitchButton(true)
                }
            }
        })
        presenter.initPreference()
        presenter.registerVolumeDownReceiver(volumeDownReceiver)
        initCameraParamsRecycler()
        relativeOrientation =
            OrientationLiveData(requireContext(), presenter.characteristics).apply {
                observe(viewLifecycleOwner, { orientation ->
                    Log.d(TAG, "Orientation changed: $orientation")
                })
            }
    }

    private fun initCameraParamsRecycler() {
        cameraParamsRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ParametersAdapter { imageId -> presenter.parametersDetails(imageId) }
        }
        (cameraParamsRecycler.adapter as ParametersAdapter).submitList(paramsImagesIdList)
    }

    private fun initializeCamera() = lifecycleScope.launch(Dispatchers.Main) {
        presenter.camera = presenter.openCamera(
            presenter.cameraManager,
            presenter.getCameraId(presenter.lensFacing),
            cameraHandler
        )
        val size = presenter.characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )!!.getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!
        imageReader =
            ImageReader.newInstance(size.width, size.height, ImageFormat.JPEG, IMAGE_BUFFER_SIZE)
        val targets = listOf(viewFinder.holder.surface, imageReader.surface)
        session = createCaptureSession(presenter.camera, targets, cameraHandler)
        val captureRequest = presenter.camera.createCaptureRequest(
            CameraDevice.TEMPLATE_PREVIEW
        ).apply {
            addTarget(viewFinder.holder.surface)
            set(CaptureRequest.CONTROL_MODE, presenter.currentCameraParams[0])
            set(CaptureRequest.CONTROL_AWB_MODE, presenter.currentCameraParams[1])
            set(CaptureRequest.SENSOR_SENSITIVITY, presenter.currentCameraParams[2])
            set(CaptureRequest.CONTROL_EFFECT_MODE, presenter.currentCameraParams[3])
            set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, presenter.currentCameraParams[4])
        }
        session.apply {
            abortCaptures()
            setRepeatingRequest(captureRequest.build(), null, cameraHandler)
        }
    }

    override fun hideParametersList() {
        ViewCompat.animate(paramsSettingsRecycler).apply {
            alpha(0F)
            duration = 300
            interpolator = AccelerateInterpolator()
            startDelay = 50
            withEndAction {
                paramsSettingsRecycler.alpha = 0F
                paramsSettingsRecycler.visibility = View.GONE
            }
        }
    }

    override fun showParametersList() {
        val showAnimation = AnimationUtils.loadAnimation(context, R.anim.setting_creating)
        paramsSettingsRecycler.startAnimation(showAnimation)
        paramsSettingsRecycler.alpha = 0.6F
        paramsSettingsRecycler.visibility = View.VISIBLE
    }

    override fun initExposureList() {
        paramsSettingsRecycler.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ExposureAdapter { value -> setNewValue(value, 4) }
            (paramsSettingsRecycler.adapter as ExposureAdapter).submitList(
                presenter.getExposureRange().toList()
            )
        }
    }

    override fun initWhiteBalanceList() {
        paramsSettingsRecycler.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = WhiteBalanceAdapter { value -> setNewValue(value, 1) }
            (paramsSettingsRecycler.adapter as WhiteBalanceAdapter).submitList(
                presenter.getWhiteBalanceValues()
            )
        }
    }

    override fun initIsoList() {
        paramsSettingsRecycler.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ExposureAdapter { value -> setNewValue(value, 2) }
            (paramsSettingsRecycler.adapter as ExposureAdapter).submitList(
                presenter.getIsoRange().toList()
            )
        }
    }

    override fun initEffectsList() {
        paramsSettingsRecycler.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = EffectsAdapter { value -> setNewValue(value, 3) }
            (paramsSettingsRecycler.adapter as EffectsAdapter).submitList(
                presenter.getEffectsValues()
            )
        }
    }

    private fun setNewValue(value: Int, index: Int) {
        updateCameraParams(value, index)
    }

    private fun updateCameraParams(value: Int, index: Int) {
        presenter.currentCameraParams[index] = value
        val captureRequest = presenter.camera.createCaptureRequest(
            CameraDevice.TEMPLATE_PREVIEW
        ).apply {
            addTarget(viewFinder.holder.surface)
            set(CaptureRequest.CONTROL_MODE, presenter.currentCameraParams[0])
            set(CaptureRequest.CONTROL_AWB_MODE, presenter.currentCameraParams[1])
            set(CaptureRequest.SENSOR_SENSITIVITY, presenter.currentCameraParams[2])
            set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, presenter.currentCameraParams[4])
            set(CaptureRequest.CONTROL_EFFECT_MODE, presenter.currentCameraParams[3])
        }
        session.apply {
            abortCaptures()
            setRepeatingRequest(captureRequest.build(), null, cameraHandler)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateCameraUi()
        updateCameraSwitchButton(true)
    }

    private suspend fun createCaptureSession(
        device: CameraDevice,
        targets: List<Surface>,
        handler: Handler? = null
    ): CameraCaptureSession = suspendCoroutine { cont ->
        device.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {

            override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

            override fun onConfigureFailed(session: CameraCaptureSession) {
                val exc = RuntimeException("Camera ${device.id} session configuration failed")
                Log.e(TAG, exc.message, exc)
                cont.resumeWithException(exc)
            }
        }, handler)
    }

    private fun updateCameraUi() {
        camera_ui_container?.let {
            camera_container.removeView(it)
        }
        val controls =
            View.inflate(requireContext(), R.layout.camera_ui_container, camera_container)
        lifecycleScope.launch(Dispatchers.IO) {
            presenter.outputDirectory.listFiles { file ->
                EXTENSION_WHITELIST.contains(file.extension.toUpperCase(Locale.ROOT))
            }?.max()?.let {
                setGalleryThumbnail(Uri.fromFile(it))
            }
        }
        controls.findViewById<ImageButton>(R.id.camera_capture_button).setOnClickListener {
            it.isEnabled = false
            lifecycleScope.launch(Dispatchers.IO) {
                takePhoto().use { result ->
                    Log.d(TAG, "Result received: $result")
                    val output = presenter.saveResult(result)
                    Log.d(TAG, "Image saved: ${output.absolutePath}")
                    setGalleryThumbnail(Uri.fromFile(output))
                    if (output.extension == "jpg") {
                        val exif = ExifInterface(output.absolutePath)
                        exif.setAttribute(
                            ExifInterface.TAG_ORIENTATION, result.orientation.toString()
                        )
                        exif.saveAttributes()
                        Log.d(TAG, "EXIF metadata saved: ${output.absolutePath}")
                    }
                }
            }
            it.isEnabled = true
        }
        controls.findViewById<ImageButton>(R.id.camera_switch_button).let {
            it.isEnabled = false
            it.setOnClickListener {
                presenter.lensFacing =
                    if (CameraCharacteristics.LENS_FACING_FRONT == presenter.lensFacing) {
                        CameraCharacteristics.LENS_FACING_BACK
                    } else {
                        CameraCharacteristics.LENS_FACING_FRONT
                    }
                try {
                    presenter.camera.close()
                } catch (exc: Throwable) {
                    Log.e(TAG, "Error closing camera", exc)
                }
                initializeCamera()
            }
        }

        controls.findViewById<ImageButton>(R.id.photo_view_button).setOnClickListener {
            if (true == presenter.outputDirectory.listFiles()?.isNotEmpty()) {
                Navigation.findNavController(
                    requireActivity(), R.id.fragment_container
                ).navigate(
                    CameraFragmentDirections
                        .actionCameraToGallery(
                            presenter.outputDirectory.absolutePath,
                            presenter.lensFacing.toString()
                        )
                )
            }
        }
        btn_presets.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(R.id.action_camera_fragment_to_presetsFragment)
        }
        btn_add.setOnClickListener {
            AddDialogFragment.show(
                requireActivity().supportFragmentManager,
                presenter.preferencesService.createParametersSet(presenter.currentCameraParams)
            )
        }
    }

    private suspend fun takePhoto(): CombinedCaptureResult = suspendCoroutine { cont ->
        @Suppress("ControlFlowWithEmptyBody")
        while (imageReader.acquireNextImage() != null) {
        }

        val imageQueue = ArrayBlockingQueue<Image>(IMAGE_BUFFER_SIZE)
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireNextImage()
            Log.d(TAG, "Image available in queue: ${image.timestamp}")
            imageQueue.add(image)
        }, imageReaderHandler)

        val captureRequest = session.device.createCaptureRequest(
            CameraDevice.TEMPLATE_STILL_CAPTURE
        ).apply {
            addTarget(imageReader.surface)
            set(CaptureRequest.CONTROL_EFFECT_MODE, presenter.currentCameraParams[3])
        }
        session.capture(captureRequest.build(), object : CameraCaptureSession.CaptureCallback() {

            override fun onCaptureStarted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                timestamp: Long,
                frameNumber: Long
            ) {
                super.onCaptureStarted(session, request, timestamp, frameNumber)
                viewFinder.post(animationTask)
            }

            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
                val resultTimestamp = result.get(CaptureResult.SENSOR_TIMESTAMP)
                Log.d(TAG, "Capture result received: $resultTimestamp")
                val exc = TimeoutException("Image dequeuing took too long")
                val timeoutRunnable = Runnable { cont.resumeWithException(exc) }
                imageReaderHandler.postDelayed(timeoutRunnable, IMAGE_CAPTURE_TIMEOUT_MILLIS)

                @Suppress("BlockingMethodInNonBlockingContext")
                lifecycleScope.launch(cont.context) {
                    while (true) {
                        val image = imageQueue.take()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                            image.format != ImageFormat.DEPTH_JPEG &&
                            image.timestamp != resultTimestamp
                        ) continue
                        Log.d(TAG, "Matching image dequeued: ${image.timestamp}")
                        imageReaderHandler.removeCallbacks(timeoutRunnable)
                        imageReader.setOnImageAvailableListener(null, null)
                        while (imageQueue.size > 0) {
                            imageQueue.take().close()
                        }
                        var rotation = relativeOrientation.value ?: 0
                        if (presenter.lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                            rotation += 180
                        }
                        val exifOrientation = computeExifOrientation(rotation, false)
                        cont.resume(
                            CombinedCaptureResult(
                                image,
                                result,
                                exifOrientation,
                                imageReader.imageFormat
                            )
                        )
                    }
                }
            }
        }, cameraHandler)
    }

    override fun updateCameraSwitchButton(isEnabled: Boolean) {
        val switchCamerasButton = camera_switch_button
        try {
            switchCamerasButton.isEnabled = presenter.hasBackCamera() && presenter.hasFrontCamera()
        } catch (exception: CameraInfoUnavailableException) {
            switchCamerasButton.isEnabled = false
        }
    }

    override fun navigateToPermissionsFragment() {
        Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
            CameraFragmentDirections.actionCameraToPermissions()
        )
    }

    companion object {
        const val TAG = "CameraXBasic"
        const val PRESET_ARGUMENT = "set preset"
        private const val IMAGE_BUFFER_SIZE: Int = 3
    }
}
