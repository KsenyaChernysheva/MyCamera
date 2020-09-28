package com.kpfu.itis.mycamera.data.models.categories

import android.hardware.camera2.CameraCharacteristics

enum class WhiteBalance(val title: String, val value: Int) {
    AUTO("auto", CameraCharacteristics.CONTROL_AWB_MODE_AUTO),
    DAYLIGHT("daylight", CameraCharacteristics.CONTROL_AWB_MODE_DAYLIGHT),
    CLOUDY_DAYLIGHT("cloudy daylight", CameraCharacteristics.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT),
    INCANDESCENT("incandescent", CameraCharacteristics.CONTROL_AWB_MODE_INCANDESCENT),
    FLUORESCENT("fluorescent", CameraCharacteristics.CONTROL_AWB_MODE_FLUORESCENT),
    WARM_FLUORESCENT("warm fluorescent", CameraCharacteristics.CONTROL_AWB_MODE_WARM_FLUORESCENT),
    SHADE("shade", CameraCharacteristics.CONTROL_AWB_MODE_SHADE),
    TWILIGHT("twilight", CameraCharacteristics.CONTROL_AWB_MODE_TWILIGHT)
}