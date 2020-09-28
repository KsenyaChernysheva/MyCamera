package com.kpfu.itis.mycamera.data.models

data class Preset(
    var name: String,
    var controlMode: Int,
    var whiteBalance: Int,
    var ISO: Int,
    var effect: Int,
    var exposure: Int
)