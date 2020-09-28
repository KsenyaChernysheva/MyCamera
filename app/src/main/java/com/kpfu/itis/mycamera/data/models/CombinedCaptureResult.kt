package com.kpfu.itis.mycamera.data.models

import android.hardware.camera2.CaptureResult
import android.media.Image
import java.io.Closeable

data class CombinedCaptureResult(
    val image: Image,
    val metadata: CaptureResult,
    val orientation: Int,
    val format: Int
) : Closeable {
    override fun close() = image.close()
}