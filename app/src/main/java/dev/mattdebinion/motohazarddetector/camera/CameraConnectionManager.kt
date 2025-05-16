package dev.mattdebinion.motohazarddetector.camera

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.arashivision.sdkcamera.camera.InstaCameraManager
import com.arashivision.sdkcamera.camera.resolution.PreviewStreamResolution
import dev.mattdebinion.motohazarddetector.networking.ConnectivityHandler


/**
 * The CameraConnectionManager class handles the interfacing between the Insta360 camera and Android.
 * Before calling, ensure the SSID and password are passed!
 *
 * @param cameraPreviewManager The camera preview manager to notify changes to during the connection state.
 * @param cameraViewModel The camera view model to notify changes to during the connection state.
 * @param context The context for the app
 * @param ssid The SSID for the hotspot
 * @param pass The password for the hotspot
 */
class CameraConnectionManager(private val cameraPreviewManager: CameraPreviewManager,
                              val cameraViewModel: CameraViewModel,
                              val context: Context,
                              private val ssid: String,
                              private val pass: String) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val connectivityHandler = ConnectivityHandler(context)

    /**
     * Connects to a camera
     *
     * @param type `CONNECT_TYPE_USB` and `CONNECT_TYPE_WIFI` supported.
     */
    fun connectCamera(type: Int) {
        // TODO: Check appropriate permissions and connectivity to camera here?
        cameraViewModel.setCameraConnectingStatusChanged(true)                                      //Set camera connecting status for UI

        try {
            /**
             * Connect to an insta360 camera via USB in this section.
             */
            if (type == InstaCameraManager.CONNECT_TYPE_USB) {
                Log.i("CameraConnectionManager", "Connecting to camera via USB...")

                mainHandler.post {
                    InstaCameraManager.getInstance().openCamera(type)
                }

                //TODO look for connection here
            /**
             * Connect to an insta360 camera via Wi-Fi in this section.
             */
            } else if (type == InstaCameraManager.CONNECT_TYPE_WIFI) {
                Log.i("CameraConnectionManager", "Connecting to camera via Wi-Fi...")

                // Step 1: Create an instance of ConnectivityHandler and pass context/credentials
                // Connection to hotspot is successful when onConnected() is called.
                connectivityHandler.connectToHotspot(ssid, pass, {

                    // Ensure connection to the camera handles on the main thread per documentation
                    // Run once onConnected() triggered from connectivityHandler
                    mainHandler.post {
                        try {
                            Log.i("CameraConnectionManager", "Opening camera...")
                            // Step 2: Open the camera once hotspot has been established.
                            InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_WIFI);

                            waitForConnectedCamera(
                                onConnected = {
                                    Log.i("CameraConnectionManager", "The supported resolutions are: ")
                                    val supportedList = InstaCameraManager.getInstance().getSupportedPreviewStreamResolution(InstaCameraManager.PREVIEW_TYPE_LIVE)
                                    Log.i("CameraConnectionManager", supportedList.toString())

                                    Log.i("CameraConnectionManager", "Launching the preview stream :3")
                                    InstaCameraManager.getInstance().setPreviewStatusChangedListener(cameraPreviewManager)
                                    InstaCameraManager.getInstance().startPreviewStream(PreviewStreamResolution.STREAM_1920_960_30FPS,InstaCameraManager.PREVIEW_TYPE_LIVE)

                                    cameraViewModel.setCameraStatusChanged(true)                            // Set the camera connected status to TRUE
                                    cameraViewModel.setCameraConnectingStatusChanged(false)                 // and set the connecting status to FALSE
                                    Log.i("CameraConnectionManager", "Camera instance opened!");
                                },
                                onTimeout = {
                                    Log.e("CameraConnectionManager", "Camera connection timed out.")
                                    cameraViewModel.setCameraConnectingStatusChanged(false)
                                }
                            )
                        } catch (e: Exception) {
                            Log.e("CameraConnectionManager", "Could not connect to the camera!")
                            cameraViewModel.setCameraConnectingStatusChanged(false)                 // Set the camera connecting status to FALSE on fail.
                            return@post;
                        }
                    }
                    // Run once onConnectFailure() triggered from connectivityHandler
                }, {
                    cameraViewModel.setCameraConnectingStatusChanged(false)                         // Set the camera connecting status to FALSE on fail
                })

            /**
             * Unsupported connection type
             */
            } else {
                Log.w("CameraConnectionManager", "Connect type code $type not supported.")
                cameraViewModel.setCameraConnectingStatusChanged(false)                             // Set the camera connecting status to FALSE on fail.
            }
        } catch (e: Exception) {
            Log.e("CameraConnectionManager", "Failed to connect to camera: $e")
            cameraViewModel.setCameraConnectingStatusChanged(false)                                 // Set the camera connecting status to FALSE on fail.
        }
    }

    fun disconnectCamera() {
        try {
            InstaCameraManager.getInstance().closeCamera()
            connectivityHandler.reconnectToPreviousNetwork()
            cameraViewModel.setCameraStatusChanged(false)
            cameraViewModel.setCameraPreviewStatusChanged(false)
        } catch (e: Exception) {
            Log.e("CameraConnectionManager", "Failed to disconnect from camera: $e")
        }
    }

    /**
     * Return if the camera is connected or not
     */
    private fun isCameraConnected(): Boolean {
        return InstaCameraManager.getInstance().cameraConnectedType != InstaCameraManager.CONNECT_TYPE_NONE
    }

    /**
     * This function continually polls the camera connection status once openCamera is called.
     */
    private fun waitForConnectedCamera(
        timeoutMillis: Long = 10000,
        intervalMillis: Long = 1000,
        onConnected: () -> Unit,
        onTimeout: () -> Unit
    ) {
        val startTime = System.currentTimeMillis()
        val handler = Handler(Looper.getMainLooper())

        val checkConnection = object : Runnable {
            override fun run() {
                Log.i("CameraConnectionManager", "Waiting for camera connection completion...")
                if (isCameraConnected()) {
                    Log.i("CameraConnectionManager", "Camera is now connected!")
                    onConnected()
                } else if (System.currentTimeMillis() - startTime < timeoutMillis) {
                    handler.postDelayed(this, intervalMillis)
                } else {
                    Log.e("CameraConnectionManager", "Camera connection timed out.")
                    onTimeout()
                }
            }
        }

        handler.post(checkConnection)
    }
}