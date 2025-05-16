package dev.mattdebinion.motohazarddetector

import android.app.Application
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SharedPreferencesManager(application: Application) {

    companion object {
        private const val PREF_NAME = "camera_prefs"
        private const val KEY_CAMERA_SSID = "X3 39FFG7.OSC"
        private const val KEY_CAMERA_PASSWORD = "88888888"
    }

    // Create or retrieve a master key for encryption
    private val masterKey = MasterKey.Builder(application)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Encrypted SharedPreferences (data is encrypted at rest)
    private val sharedPreferences = EncryptedSharedPreferences.create(
        application,
        PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Save SSID
    fun setCameraSSID(ssid: String) {
        sharedPreferences.edit { putString(KEY_CAMERA_SSID, ssid) }
    }

    // Save Password
    fun setCameraPassword(password: String) {
        sharedPreferences.edit { putString(KEY_CAMERA_PASSWORD, password) }
    }

    // Get SSID
    fun getCameraSSID(): String {
        return sharedPreferences.getString(KEY_CAMERA_SSID, "") ?: ""
    }

    // Get Password
    fun getCameraPassword(): String {
        return sharedPreferences.getString(KEY_CAMERA_PASSWORD, "") ?: ""
    }
}
