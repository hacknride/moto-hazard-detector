package dev.mattdebinion.motohazarddetector.camera.data

data class CameraProperties(
    val nickname: String,
    val make: String,
    val model: String,
    val ssid: String,
    val password: String,
    val previewResolution: String,
    val previewFrameRate: Int,
    val liveResolution: String,
    val liveFrameRate: Int,
    val liveBitRate: Int,
)