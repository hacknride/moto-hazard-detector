package dev.mattdebinion.motohazarddetector.ai.data

import android.graphics.Rect

data class Hazard(
    val type: HazardType,
    val confidence: Float,
    val timestamp: Long,
    val boundingBox: Rect? = null
)

