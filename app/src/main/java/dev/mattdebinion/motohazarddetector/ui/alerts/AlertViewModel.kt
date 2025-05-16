package dev.mattdebinion.motohazarddetector.ui.alerts

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.mattdebinion.motohazarddetector.ui.alerts.data.HazardAlert
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlertViewModel : ViewModel() {
    private val _alerts = MutableLiveData<MutableList<HazardAlert>>(mutableListOf())
    val alerts: LiveData<MutableList<HazardAlert>> = _alerts

    fun postAlert(title: String, message: String, severity: Int) {
        Log.i("AlertViewModel", "Posting alert: $message")
        val now = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val newAlert = HazardAlert(
            title = title,
            timestamp = now,
            severity = severity,
            description = message
        )

        // Create new list based on existing data
        val currentList = _alerts.value ?: mutableListOf()
        val updatedList = mutableListOf<HazardAlert>().apply {
            addAll(currentList)
            add(0, newAlert)
        }

        // Use postValue since this may be called from a background thread
        _alerts.postValue(updatedList)
    }

}
