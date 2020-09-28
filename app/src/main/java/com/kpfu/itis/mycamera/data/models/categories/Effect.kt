package com.kpfu.itis.mycamera.data.models.categories

import android.hardware.camera2.CaptureRequest

enum class Effect(val title: String, val value: Int) {
    OFF("off", CaptureRequest.CONTROL_EFFECT_MODE_OFF),
    NEGATIVE("negative", CaptureRequest.CONTROL_EFFECT_MODE_NEGATIVE),
    AQUA("aqua", CaptureRequest.CONTROL_EFFECT_MODE_AQUA),
    MONO("mono", CaptureRequest.CONTROL_EFFECT_MODE_MONO),
    BLACKBOARD("blackboard", CaptureRequest.CONTROL_EFFECT_MODE_BLACKBOARD),
    POSTERIZE("posterize", CaptureRequest.CONTROL_EFFECT_MODE_POSTERIZE),
    SOLARIZE("solarize", CaptureRequest.CONTROL_EFFECT_MODE_SOLARIZE),
    SEPIA("sepia", CaptureRequest.CONTROL_EFFECT_MODE_SEPIA),
    WHITEBOARD("whiteboard", CaptureRequest.CONTROL_EFFECT_MODE_WHITEBOARD)
}