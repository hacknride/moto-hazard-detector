package dev.mattdebinion.motohazarddetector.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.mattdebinion.motohazarddetector.permissions.AppPermissionManager
import dev.mattdebinion.motohazarddetector.databinding.FragmentSettingsLiveBinding

class LiveFragment : Fragment(), AppPermissionManager.PermissionActions {

    private var _binding: FragmentSettingsLiveBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsLiveBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPermissionGranted(requestCode: Int) {
        TODO("Not yet implemented")
    }

    override fun onPermissionDenied(requestCode: Int) {
        TODO("Not yet implemented")
    }
}