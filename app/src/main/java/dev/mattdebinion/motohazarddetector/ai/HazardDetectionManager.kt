package dev.mattdebinion.motohazarddetector.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import dev.mattdebinion.motohazarddetector.ui.alerts.AlertViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The Hazard Detection Manager hooks into the Camera Preview Manager to receive frames from the camera
 * for inferencing and sends alerts to the Alert View Model.
 */
class HazardDetectionManager(
    context: Context,
    private val alertViewModel: AlertViewModel
) {

    // Log purposes
    companion object {
        private const val TAG = "HazardDetectionManager"
    }

    private val hazardModel: HazardModelCaller = HazardModelCaller(context)

    /**
     * Analyzes a single frame to detect if a hazard is present and updates the alert UI accordingly.
     */
    fun analyzeFrame(frame: Bitmap) {
        CoroutineScope(Dispatchers.Default). launch {
            val result = withContext(Dispatchers.Default) {
                hazardModel.detectHazard(frame)
            }

            if (result.isHazard) {
                Log.i(TAG, "Hazard detected: ${result.description}")
                alertViewModel.postAlert("Hazard Alert", result.description, 2)
            } else {
                Log.d(TAG, "No hazard detected in this frame.")
            }
        }
    }
}
