package dev.mattdebinion.motohazarddetector.camera

import android.content.Context
import android.util.Log
import androidx.core.view.drawToBitmap
import androidx.lifecycle.Lifecycle
import dev.mattdebinion.motohazarddetector.ui.alerts.AlertViewModel
import com.arashivision.sdkcamera.camera.InstaCameraManager
import com.arashivision.sdkcamera.camera.callback.IPreviewStatusListener
import com.arashivision.sdkcamera.camera.preview.VideoData
import com.arashivision.sdkmedia.player.capture.CaptureParamsBuilder
import com.arashivision.sdkmedia.player.capture.InstaCapturePlayerView
import com.arashivision.sdkmedia.player.listener.PlayerViewListener
import dev.mattdebinion.motohazarddetector.ai.HazardDetectionManager

/**
 * Camera preview handler
 *
 * @param cameraViewModel The camera view model to update changes to for the UI
 * @param alertViewModel The alert view model to update changes to for the UI
 * @constructor Create empty Camera preview handler
 */
class CameraPreviewManager(private val context: Context, private val cameraViewModel: CameraViewModel, private val alertViewModel: AlertViewModel) : IPreviewStatusListener {

    private lateinit var capturePlayerView: InstaCapturePlayerView
    private lateinit var hazardDetectionManager: HazardDetectionManager

    /**
     * Binds the InstaCapturePlayerView to a lifecycle
     *
     * @param lifecycle The lifecycle for the activity or fragment
     */
    fun bindLifecycle(feedView: InstaCapturePlayerView, lifecycle: Lifecycle) {
        capturePlayerView = feedView
        capturePlayerView.setLifecycle(lifecycle)
    }


    // TODO
    override fun onOpening() {
        super.onOpening()

        cameraViewModel.setCameraPreviewConnectingStatusChanged(true)
        Log.i("CameraPreviewManager", "The preview is opening...")
    }

    //TODO
    override fun onOpened() {
        super.onOpened()
        Log.i("CameraPreviewManager", "The camera has opened!")
        hazardDetectionManager = HazardDetectionManager(context, alertViewModel)

        cameraViewModel.setCameraPreviewConnectingStatusChanged(true)                               // Set the preview connecting status to TRUE
        InstaCameraManager.getInstance().setStreamEncode()

        // Attach a listener to the player
        capturePlayerView.setPlayerViewListener(object : PlayerViewListener {
            override fun onLoadingFinish() {
                InstaCameraManager.getInstance().setPipeline(capturePlayerView.pipeline)

                cameraViewModel.setCameraPreviewConnectingStatusChanged(false)                      // Set the preview connecting status to FALSE
                cameraViewModel.setCameraPreviewStatusChanged(true)                                 // then set the preview connected status to TRUE
                Log.i("CameraPreviewHandler", "The preview has loaded successfully.")
                alertViewModel.postAlert("Camera Connected", "The camera is now connected and watching the road. Safe travels!", 0)
            }

            override fun onReleaseCameraPipeline() {
                Log.i("CameraPreviewHandler", "The preview pipeline has ended.")
                alertViewModel.postAlert("Camera Disconnected Safely", "The camera is now disconnected. Welcome to your destination!", 0)
                InstaCameraManager.getInstance().setPipeline(null)
                capturePlayerView.keepScreenOn = false

                cameraViewModel.setCameraPreviewStatusChanged(false)                                // Set the preview connected status to FALSE
            }

            override fun onFail(errorCode: Int, errorMsg: String?) {
                super.onFail(errorCode, errorMsg)
                Log.i("CameraPreviewHandler", "The preview pipeline has failed to load for this reason: $errorMsg")
            }
        })

        capturePlayerView.prepare(createParams())
        capturePlayerView.play()
        capturePlayerView.keepScreenOn = true

    }

    override fun onIdle() {
        super.onIdle()

        cameraViewModel.setCameraPreviewStatusChanged(false)                                        // Set the preview connected status to FALSE
        capturePlayerView.destroy()
        capturePlayerView.keepScreenOn = false
        //cameraConnectionManager.disconnectCamera()
    }

    override fun onError() {
        super.onError()

        cameraViewModel.setCameraPreviewStatusChanged(false)                                        // Set the preview connected status to FALSE
        Log.w("CameraPreviewManager", "An error occurred in the camera.")
        alertViewModel.postAlert("Camera Error", "An error occurred retrieving the feed from your camera. Please reconnect!", 2)
    }

    override fun onVideoData(videoData: VideoData?) {
        super.onVideoData(videoData)
        val bitmap = capturePlayerView.drawToBitmap()

        if (bitmap.width > 0 && bitmap.height > 0) {
            hazardDetectionManager.analyzeFrame(bitmap)
        }
    }

    private fun createParams(): CaptureParamsBuilder {
        val builder = CaptureParamsBuilder()
            .setCameraType(InstaCameraManager.getInstance().cameraType)
            .setMediaOffset(InstaCameraManager.getInstance().mediaOffset)
            .setMediaOffsetV2(InstaCameraManager.getInstance().mediaOffsetV2)
            .setMediaOffsetV3(InstaCameraManager.getInstance().mediaOffsetV3)
            .setCameraSelfie(InstaCameraManager.getInstance().isCameraSelfie)
            .setGyroTimeStamp(InstaCameraManager.getInstance().gyroTimeStamp)
            .setBatteryType(InstaCameraManager.getInstance().batteryType)
            .setResolutionParams(1920, 1080, 30)

        return builder
    }

}