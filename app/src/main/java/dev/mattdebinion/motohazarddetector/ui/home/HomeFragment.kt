package dev.mattdebinion.motohazarddetector.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.arashivision.sdkcamera.camera.InstaCameraManager
import com.arashivision.sdkcamera.camera.resolution.PreviewStreamResolution
import com.arashivision.sdkmedia.player.capture.InstaCapturePlayerView
import dev.mattdebinion.motohazarddetector.R
import dev.mattdebinion.motohazarddetector.SharedPreferencesManager
import dev.mattdebinion.motohazarddetector.ai.HazardDetectionManager
import dev.mattdebinion.motohazarddetector.camera.CameraConnectionManager
import dev.mattdebinion.motohazarddetector.camera.CameraPreviewManager
import dev.mattdebinion.motohazarddetector.camera.CameraViewModel
import dev.mattdebinion.motohazarddetector.databinding.FragmentHomeBinding
import dev.mattdebinion.motohazarddetector.ui.alerts.AlertLogAdapter
import dev.mattdebinion.motohazarddetector.ui.alerts.AlertViewModel
import dev.mattdebinion.motohazarddetector.ui.settings.GeneralViewModel

/**
 * The Home fragment displays a feed when connected to the camera as well as the camera status.
 *
 * @constructor Creates the default Home fragment
 */
class HomeFragment : Fragment(), ConnectTypeDialogFragment.ConnectTypeDialogListener {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // View models that need to be checked by the camera
    private val generalViewModel: GeneralViewModel by activityViewModels()
    private val cameraViewModel: CameraViewModel by activityViewModels()
    private val alertViewModel: AlertViewModel by activityViewModels()

    // Set the adapters to be used by the HomeFragment
    private lateinit var alertLogAdapter: AlertLogAdapter

    // The camera connection manager and preview manager. Related variables included
    private lateinit var cameraConnectionManager: CameraConnectionManager
    private lateinit var cameraPreviewManager: CameraPreviewManager
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var runnable2: Runnable
    private var startTime: Long = 0

    // The CapturePlayerView
    private lateinit var capturePlayerView: InstaCapturePlayerView

    // Shared Preferences
    private lateinit var sharedPreferencesManager: SharedPreferencesManager



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize the capturePlayerView and CameraPreviewManager
        capturePlayerView = binding.capturePlayerView
        cameraPreviewManager = CameraPreviewManager(requireContext(), cameraViewModel, alertViewModel)
        cameraPreviewManager.bindLifecycle(capturePlayerView, viewLifecycleOwner.lifecycle)
        InstaCameraManager.getInstance().setPreviewStatusChangedListener(cameraPreviewManager)
        handler = Handler(Looper.getMainLooper())

        val alertRecyclerView = binding.alertLogRecycler
        alertLogAdapter = AlertLogAdapter(emptyList())
        alertRecyclerView.adapter = alertLogAdapter
        alertRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up the hazard detection AI
        //hazardDetectionManager = HazardDetectionManager(requireContext(), alertViewModel, cameraPreviewManager, viewLifecycleOwner.lifecycleScope)


        setFragmentListeners()      // Set the fragment listeners to update the UI
        setButtonListeners()        // Set the button listeners to update the UI

        return binding.root
    }



    override fun onDestroyView() {
        super.onDestroyView()
        InstaCameraManager.getInstance().setPreviewStatusChangedListener(null)
        //hazardDetectionManager.close()
        _binding = null
    }


    override fun onResume() {
        super.onResume()

        if (cameraViewModel.isCameraConnected.value == true) {
            Log.i("HomeFragment", "Camera is already connected. Restarting preview stream.")

            capturePlayerView.post {
                cameraPreviewManager.bindLifecycle(capturePlayerView, viewLifecycleOwner.lifecycle)
                InstaCameraManager.getInstance().setPreviewStatusChangedListener(cameraPreviewManager)
                InstaCameraManager.getInstance().startPreviewStream(
                    PreviewStreamResolution.STREAM_1920_960_30FPS,
                    InstaCameraManager.PREVIEW_TYPE_LIVE
                )
            }
        }
    }

    override fun onConfirmClick(dialog: DialogFragment, connectType: Int) {
        Log.i("HomeFragment", "Confirmed click for $connectType")

        if (cameraViewModel.isCameraConnected.value == false) {
            val ssid = generalViewModel.cameraSSID.value.toString()
            val password = generalViewModel.cameraPass.value.toString()

            cameraConnectionManager = CameraConnectionManager(cameraPreviewManager, cameraViewModel, requireContext(), ssid, password)
            cameraConnectionManager.connectCamera(connectType)
        }
    }

    override fun onCancelClick(dialog: DialogFragment) {
        Log.i("HomeFragment", "User cancelled connection dialog.")
    }

    /**
     * setFragmentObservers sets all the UI elements to their respective observers, if necessary.
     */
    private fun setFragmentListeners() {

        // If the camera is connected, update the button icon and text accordingly
        cameraViewModel.isCameraConnected.observe(viewLifecycleOwner, Observer { isCameraConnected ->
            binding.buttonConnection.isEnabled = true

            if(isCameraConnected) {
                binding.buttonConnection.text = getString(R.string.button_text_disconnect)
                binding.buttonConnection.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_material_link_off, 0, 0, 0)
                binding.capturePlayerFrameStatus.visibility = View.INVISIBLE
                binding.capturePlayerView.visibility = View.VISIBLE
            } else {
                binding.buttonConnection.text = getString(R.string.button_text_connect)
                binding.buttonConnection.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_material_link, 0, 0, 0)
                binding.capturePlayerFrameStatus.visibility = View.VISIBLE
                binding.capturePlayerView.visibility = View.INVISIBLE
            }
        })

        cameraViewModel.isCameraConnecting.observe(viewLifecycleOwner, Observer { isCameraConnecting ->

            var currentIconIndex = 0
            val icons = listOf(
                R.drawable.ic_material_wifi_bar_0,
                R.drawable.ic_material_wifi_bar_1,
                R.drawable.ic_material_wifi_bar_2,
                R.drawable.ic_material_wifi_bar_3,
                R.drawable.ic_material_wifi_bar_4
            )
            if(isCameraConnecting) {
                binding.buttonConnection.isEnabled = false
                binding.capturePlayerFrameStatusText.text = "Camera is connecting..."
                runnable2 = object : Runnable {
                    override fun run() {
                        binding.capturePlayerFrameStatusIcon.setImageResource(icons[currentIconIndex])
                        currentIconIndex = (currentIconIndex + 1) % icons.size

                        handler.postDelayed(this, 500)
                    }
                }
                handler.post(runnable2)
            } else {
                if(::runnable2.isInitialized) {
                    binding.capturePlayerFrameStatusIcon.setImageResource(R.drawable.ic_material_signal_disconnected)
                    binding.buttonConnection.isEnabled = true
                    binding.capturePlayerFrameStatusText.text = "Camera could not connect."
                    handler.removeCallbacks(runnable2)
                }
            }
        })

        alertViewModel.alerts.observe(viewLifecycleOwner) { alerts ->
            alertLogAdapter.updateAlerts(alerts)
        }

        // TODO the HomeViewModel for the lock/unlock button!
    }

    private fun setButtonListeners() {
        binding.buttonConnection.setOnClickListener {
            if (cameraViewModel.isCameraConnected.value == true) {
                // Directly disconnect without dialog
                if (::cameraConnectionManager.isInitialized) {
                    cameraConnectionManager.disconnectCamera()
                } else {
                    Log.w("HomeFragment", "Attempted to disconnect, but cameraConnectionManager was not initialized.")
                }
            } else {
                // Show connection type dialog
                val promptConnectType = ConnectTypeDialogFragment()
                promptConnectType.setListener(this)
                promptConnectType.show(parentFragmentManager, "ConnectTypeDialogFragment")
            }
        }
    }

}