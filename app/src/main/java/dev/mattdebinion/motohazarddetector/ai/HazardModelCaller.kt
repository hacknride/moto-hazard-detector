package dev.mattdebinion.motohazarddetector.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.scale
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor

data class HazardDetectionResult(val isHazard: Boolean, val description: String)

/**
 * This class is responsible for running inference on frames using the hazard detection model.
 */
class HazardModelCaller(context: Context) {

    private val model: Module
    private val inputImageSize = 640
    private val confidenceThreshold = 0.85f

    // Log purposes
    companion object {
        private const val TAG = "HazardModelCaller"
    }

    /**
     * Initialize the model.
     */
    init {
        model = Module.load(assetFilePath(context, "best.torchscript"))
    }

    /**
     * Run inference on a frame and return the result.
     */
    fun detectHazard(bitmap: Bitmap): HazardDetectionResult {
        val resizedBitmap = bitmap.scale(inputImageSize, inputImageSize)
        val inputTensor = bitmapToFloatTensor(resizedBitmap)

        val output = model.forward(IValue.from(inputTensor)).toTensor()
        val outputData = output.dataAsFloatArray
        val numDetections = outputData.size / 6

        Log.d(TAG, "Output shape: ${output.shape().contentToString()}")
        Log.d(TAG, "Sample output: ${outputData.take(20).joinToString(", ")}")

        for (i in 0 until numDetections) {
            val base = i * 6
            val rawConfidence = outputData[base + 4]
            val classId = outputData[base + 5]

            // Clamp confidence between 0 and 1
            val confidence = rawConfidence.coerceIn(0f, 1f)

            if (confidence > confidenceThreshold) {
                val confidencePercent = "%.2f".format(confidence * 100)
                Log.i(TAG, "Hazard detected (class $classId) with $confidencePercent% confidence")
                return HazardDetectionResult(
                    isHazard = true,
                    description = "Hazard detected (class $classId) with $confidencePercent% confidence"
                )
            }
        }

        Log.i(TAG, "No hazard detected in current frame")
        return HazardDetectionResult(isHazard = false, description = "No hazard detected")
    }

    private fun bitmapToFloatTensor(bitmap: Bitmap): Tensor {
        val floatValues = FloatArray(3 * inputImageSize * inputImageSize)
        val pixels = IntArray(inputImageSize * inputImageSize)
        bitmap.getPixels(pixels, 0, inputImageSize, 0, 0, inputImageSize, inputImageSize)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = ((pixel shr 16) and 0xFF) / 255f
            val g = ((pixel shr 8) and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f

            val x = i % inputImageSize
            val y = i / inputImageSize
            val offset = y * inputImageSize + x

            floatValues[offset] = r
            floatValues[offset + inputImageSize * inputImageSize] = g
            floatValues[offset + 2 * inputImageSize * inputImageSize] = b
        }

        return Tensor.fromBlob(floatValues, longArrayOf(1, 3, inputImageSize.toLong(), inputImageSize.toLong()))
    }

    private fun assetFilePath(context: Context, assetName: String): String {
        val file = java.io.File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }

        context.assets.open(assetName).use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return file.absolutePath
    }
}
