package com.kpfu.itis.mycamera.domain

import android.util.Log
import com.kpfu.itis.mycamera.data.models.CombinedCaptureResult
import com.kpfu.itis.mycamera.presentation.fragments.CameraFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class StorageService {

    suspend fun saveResult(result: CombinedCaptureResult, outputDirectory: File): File =
        suspendCoroutine { cont ->
            val bytes = createByteArray(result.image.planes[0].buffer)
            try {
                val output = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
                FileOutputStream(output).write(bytes)
                cont.resume(output)
            } catch (exc: IOException) {
                Log.e(CameraFragment.TAG, "Unable to write JPG image to file", exc)
                cont.resumeWithException(exc)
            }
        }

    fun createFile(baseFolder: File, format: String, extension: String) =
        File(
            baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension
        )

    fun createByteArray(buffer: ByteBuffer): ByteArray =
        ByteArray(buffer.remaining()).apply { buffer.get(this) }

    companion object {
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
    }
}