package dev.mattdebinion.motohazarddetector.ui.settings

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import dev.mattdebinion.motohazarddetector.R
import dev.mattdebinion.motohazarddetector.SharedPreferencesManager
import dev.mattdebinion.motohazarddetector.permissions.AppPermissionManager
import dev.mattdebinion.motohazarddetector.databinding.FragmentSettingsAppBinding
import dev.mattdebinion.motohazarddetector.permissions.PermissionGroup
import dev.mattdebinion.motohazarddetector.permissions.PermissionsViewModel

/**
 * The GeneralFragment (general settings) allows updating of specific app level settings, such as
 * permissions.
 *
 * @constructor Create the GeneralFragment settings page
 */
class GeneralFragment : Fragment(), AppPermissionManager.PermissionActions {

    private lateinit var appPermissionManager: AppPermissionManager
    private lateinit var sharedPreferencesManager: SharedPreferencesManager



    private var _binding: FragmentSettingsAppBinding? = null
    private val binding get() = _binding!!

    private val permissionsViewModel: PermissionsViewModel by activityViewModels()
    private val generalViewModel: GeneralViewModel by activityViewModels()

    private var isPassVisible = false

    /**
     * This override function instantiates the permission manager and the SharedPreferencesManager.
     *
     * @param context
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        appPermissionManager = AppPermissionManager.getInstance(context)
        sharedPreferencesManager = SharedPreferencesManager(context.applicationContext as Application)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsAppBinding.inflate(inflater, container, false)
        appPermissionManager.setPermissionActionsListener(this)

        // Set up fragment observers and button listeners.
        setFragmentObservers()
        setButtonListeners()

        // Check all permissions for the App Permissions table
        checkPermissions()

        // Load saved credentials from SharedPreferences
        val savedSSID = sharedPreferencesManager.getCameraSSID()
        val savedPassword = sharedPreferencesManager.getCameraPassword()

        // Set in ViewModel
        generalViewModel.setCameraSSID(savedSSID)
        generalViewModel.setCameraPassword(savedPassword)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    /**
     * When permission is granted, ensure it changes the appropriate label within the table.
     *
     * @param requestCode The request code of the granted permission
     */
    override fun onPermissionGranted(requestCode: Int) {
        when (requestCode) {
            100 -> permissionsViewModel.setBluetoothPermissionGranted(true)
            101 -> permissionsViewModel.setWifiPermissionGranted(true)
            200 -> permissionsViewModel.setLocationPermissionGranted(true)
            201 -> permissionsViewModel.setMicrophonePermissionGranted(true)
            900 -> permissionsViewModel.setUsbPermissionGranted(true)
        }
    }

    override fun onPermissionDenied(requestCode: Int) {
        when (requestCode) {
            100 -> permissionsViewModel.setBluetoothPermissionGranted(false)
            101 -> permissionsViewModel.setWifiPermissionGranted(false)
            200 -> permissionsViewModel.setLocationPermissionGranted(false)
            201 -> permissionsViewModel.setMicrophonePermissionGranted(false)
            900 -> permissionsViewModel.setUsbPermissionGranted(false)
        }
    }


    /**
     * Check permissions and sets the ViewModel appropriately.
     *
     */
    private fun checkPermissions() {
        val bluetoothGranted = appPermissionManager.checkPermissionGroup(PermissionGroup.BLUETOOTH)
        val locationGranted = appPermissionManager.checkPermissionGroup(PermissionGroup.LOCATION)
        val microphoneGranted = appPermissionManager.checkPermissionGroup(PermissionGroup.MICROPHONE)
        val usbGranted = appPermissionManager.checkPermissionGroup(PermissionGroup.USB)
        val wifiGranted = appPermissionManager.checkPermissionGroup(PermissionGroup.WIFI)

        permissionsViewModel.setUsbPermissionGranted(usbGranted)
        permissionsViewModel.setMicrophonePermissionGranted(microphoneGranted)
        permissionsViewModel.setLocationPermissionGranted(locationGranted)
        permissionsViewModel.setBluetoothPermissionGranted(bluetoothGranted)
        permissionsViewModel.setWifiPermissionGranted(wifiGranted)
    }

    /**
     * Sets button listeners for this fragment
     */
    private fun setButtonListeners() {
        binding.permissionsTableButtonBluetooth.setOnClickListener {
            appPermissionManager.requestPermission(requireActivity(), PermissionGroup.BLUETOOTH)
        }

        binding.permissionsTableButtonLocation.setOnClickListener {
            appPermissionManager.requestPermission(requireActivity(), PermissionGroup.LOCATION)
        }

        binding.permissionsTableButtonMicrophone.setOnClickListener {
            appPermissionManager.requestPermission(requireActivity(), PermissionGroup.MICROPHONE)
        }

        binding.permissionsTableButtonUSB.setOnClickListener {
            appPermissionManager.requestPermission(requireActivity(), PermissionGroup.USB)
        }

        binding.permissionsTableButtonWiFi.setOnClickListener {
            appPermissionManager.requestPermission(requireActivity(), PermissionGroup.WIFI)
        }

        // The password toggle
        binding.settingsPasswordToggle.setOnClickListener {
            isPassVisible = !isPassVisible

            if(isPassVisible) {
                binding.settingsCameraPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.settingsPasswordToggle.setImageResource(R.drawable.ic_material_visibility)
            } else {
                binding.settingsCameraPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.settingsPasswordToggle.setImageResource(R.drawable.ic_material_visibility_off)
            }

            binding.settingsCameraPassword.setSelection(binding.settingsCameraPassword.text.length)
        }

        // When pressed, locks this button and unlocks text fields and confirm button
        // TODO find a way to save information entered into text fields
        // TODO ensure information is not saved when backing out of the fragment.
        // TODO pull information from these EditTexts to connect via WIFI!

        binding.credentialButtonEdit.setOnClickListener {
            generalViewModel.toggleCredentialEditing()
        }

        binding.credentialButtonConfirm.setOnClickListener {
            val ssid = binding.settingsCameraSsid.text.toString()
            val password = binding.settingsCameraPassword.text.toString()

            // Save to SharedPreferences
            sharedPreferencesManager.setCameraSSID(ssid)
            sharedPreferencesManager.setCameraPassword(password)

            // Update ViewModel
            generalViewModel.setCameraSSID(ssid)
            generalViewModel.setCameraPassword(password)

            generalViewModel.toggleCredentialEditing()

        }
    }

    /**
     * setFragmentObservers sets all the UI elements to their respective observers, if necessary.
     */
    private fun setFragmentObservers() {

        // The following set the observation states of the Permissions Table
        permissionsViewModel.bluetoothPermissionGranted.observe(viewLifecycleOwner, Observer { granted ->
            if(granted) {
                binding.permissionTableIconBluetooth.setImageResource(R.drawable.ic_material_check_circle)
                binding.permissionTableIconBluetooth.setColorFilter(ContextCompat.getColor(requireContext(), R.color.DARK_icon_success_green), android.graphics.PorterDuff.Mode.SRC_IN)
                binding.permissionsTableButtonBluetooth.isEnabled = false
            } else {
                binding.permissionTableIconBluetooth.setImageResource(R.drawable.ic_material_error)
                binding.permissionTableIconBluetooth.setColorFilter(ContextCompat.getColor(requireContext(), R.color.DARK_icon_error_red), android.graphics.PorterDuff.Mode.SRC_IN)
                binding.permissionsTableButtonBluetooth.isEnabled = true
            }
        })
        permissionsViewModel.locationPermissionGranted.observe(viewLifecycleOwner, Observer { granted ->
            if(granted) {
                binding.permissionTableIconLocation.setImageResource(R.drawable.ic_material_check_circle)
                binding.permissionTableIconLocation.setColorFilter(ContextCompat.getColor(requireContext(), R.color.DARK_icon_success_green), android.graphics.PorterDuff.Mode.SRC_IN)
                binding.permissionsTableButtonLocation.isEnabled = false
            } else {
                binding.permissionTableIconLocation.setImageResource(R.drawable.ic_material_error)
                binding.permissionTableIconLocation.setColorFilter(ContextCompat.getColor(requireContext(), R.color.DARK_icon_error_red), android.graphics.PorterDuff.Mode.SRC_IN)
                binding.permissionsTableButtonLocation.isEnabled = true
            }
        })
        permissionsViewModel.microphonePermissionGranted.observe(viewLifecycleOwner, Observer { granted ->
            if(granted) {
                binding.permissionTableIconMicrophone.setImageResource(R.drawable.ic_material_check_circle)
                binding.permissionTableIconMicrophone.setColorFilter(ContextCompat.getColor(requireContext(), R.color.DARK_icon_success_green), android.graphics.PorterDuff.Mode.SRC_IN)
                binding.permissionsTableButtonMicrophone.isEnabled = false
            } else {
                binding.permissionTableIconMicrophone.setImageResource(R.drawable.ic_material_error)
                binding.permissionTableIconMicrophone.setColorFilter(ContextCompat.getColor(requireContext(), R.color.DARK_icon_error_red), android.graphics.PorterDuff.Mode.SRC_IN)
                binding.permissionsTableButtonMicrophone.isEnabled = true
            }
        })
        permissionsViewModel.usbPermissionGranted.observe(viewLifecycleOwner, Observer { granted ->
            if(granted) {
                binding.permissionTableIconUSB.setImageResource(R.drawable.ic_material_check_circle)
                binding.permissionTableIconUSB.setColorFilter(ContextCompat.getColor(requireContext(), R.color.DARK_icon_success_green), android.graphics.PorterDuff.Mode.SRC_IN)
                binding.permissionsTableButtonUSB.isEnabled = false
            } else {
                binding.permissionTableIconUSB.setImageResource(R.drawable.ic_material_error)
                binding.permissionTableIconUSB.setColorFilter(ContextCompat.getColor(requireContext(), R.color.DARK_icon_error_red), android.graphics.PorterDuff.Mode.SRC_IN)
                binding.permissionsTableButtonUSB.isEnabled = true
            }
        })
        permissionsViewModel.wifiPermissionGranted.observe(viewLifecycleOwner, Observer { granted ->
            if(granted) {
                binding.permissionTableIconWiFi.setImageResource(R.drawable.ic_material_check_circle)
                binding.permissionTableIconWiFi.setColorFilter(ContextCompat.getColor(requireContext(), R.color.DARK_icon_success_green), android.graphics.PorterDuff.Mode.SRC_IN)
                binding.permissionsTableButtonWiFi.isEnabled = false
            } else {
                binding.permissionTableIconWiFi.setImageResource(R.drawable.ic_material_error)
                binding.permissionTableIconWiFi.setColorFilter(ContextCompat.getColor(requireContext(), R.color.DARK_icon_error_red), android.graphics.PorterDuff.Mode.SRC_IN)
                binding.permissionsTableButtonWiFi.isEnabled = true
            }
        })

        // This updates the UI components based on the isEditingCredential LiveData boolean state.
        generalViewModel.isEditingCredential.observe(viewLifecycleOwner, Observer { isEditing ->

            // If editing, switch the edit button to cancel, enable input fields, password visibility, and confirm button
            if(isEditing) {
                binding.credentialButtonEdit.text = getString(R.string.button_text_cancel)
                binding.credentialButtonEdit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_material_cancel, 0, 0, 0)
                binding.credentialButtonConfirm.isEnabled = true
                binding.settingsCameraSsid.isEnabled = true
                binding.settingsCameraPassword.isEnabled = true
                binding.settingsPasswordToggle.isEnabled = true
            // Otherwise, default to previously saved creds, switch cancel button to edit disable input fields,
            // password visibility, and confirm button
            } else {
                binding.credentialButtonEdit.text = getString(R.string.button_text_edit)
                binding.credentialButtonEdit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_material_edit, 0, 0, 0)
                binding.credentialButtonConfirm.isEnabled = false
                binding.settingsCameraSsid.isEnabled = false
                binding.settingsCameraPassword.isEnabled = false

                if(isPassVisible) {
                    binding.settingsPasswordToggle.performClick()
                }
                binding.settingsPasswordToggle.isEnabled = false

                binding.settingsCameraSsid.setText(generalViewModel.cameraSSID.value)
                binding.settingsCameraPassword.setText(generalViewModel.cameraPass.value)

            }
        })

        // This updates the text fields on the camera credentials
        generalViewModel.cameraSSID.observe(viewLifecycleOwner, Observer { ssid ->
            binding.settingsCameraSsid.setText(ssid)
        })
        generalViewModel.cameraPass.observe(viewLifecycleOwner, Observer { pass ->
            binding.settingsCameraPassword.setText(pass)
        })
    }
}