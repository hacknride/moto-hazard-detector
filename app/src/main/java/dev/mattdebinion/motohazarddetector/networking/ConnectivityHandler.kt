package dev.mattdebinion.motohazarddetector.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * The ConnectivityHandler class handles Android connection state.
 */
class ConnectivityHandler(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager;
    private var networkCallback: ConnectivityManager.NetworkCallback? = null;
    private var onMobileData = false;

    /**
     * Attempt to connect to a given hotspot.
     */
    fun connectToHotspot(ssid: String, password: String, onConnected: () -> Unit, onConnectFailure: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        var callbackInvoked = false
        var elapsedTime = 0


        try {
            saveCurrentNetworkState()
        } catch (e: Exception) {
            Log.e("ConnectivityHandler", "Could not save the current network state, will be unable to fallback to previous connection method.");
        }

        // Build the hotspot specifier
        val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()

        // Build the network request
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()

        // Handle case when the specified hotspot does not connect after a duration of time.
        val timeoutRunnable = Runnable {
            if (!callbackInvoked) {
                callbackInvoked = true
                Log.e("ConnectivityHandler", "Connection to hotspot timed out.")
                unregisterNetworkCallback() // Clean up
                onConnectFailure()
            }
        }

        val statusLogger = object : Runnable {
            override fun run() {
                if (!callbackInvoked) {
                    Log.i("ConnectivityHandler", "Waiting for hotspot... ($elapsedTime s)")
                    elapsedTime++
                    handler.postDelayed(this, 1000)
                }
            }
        }

        // Initiate time
        handler.postDelayed(timeoutRunnable, 10_000L)
        handler.post(statusLogger)

        // Wrap real callbacks to guard against timeout overlap
        val safeOnConnected = {
            if (!callbackInvoked) {
                callbackInvoked = true
                handler.removeCallbacks(timeoutRunnable)
                handler.removeCallbacks(statusLogger)
                onConnected()
            }
        }

        val safeOnFailure = {
            if (!callbackInvoked) {
                callbackInvoked = true
                handler.removeCallbacks(timeoutRunnable)
                handler.removeCallbacks(statusLogger)
                onConnectFailure()
            }
        }

        registerNetworkCallback(networkRequest, safeOnConnected, safeOnFailure);
    }

    /**
     * Saves the current network state of the Android device.
     */
    private fun saveCurrentNetworkState() {

        // Get the current active network and it's capabilities.
        val activeNetwork: Network? = connectivityManager.activeNetwork;
        val netCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

        if (netCapabilities != null && netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            onMobileData = true;
            Log.i("ConnectivityHandler", "Mobile data was being used, saved state.");
        } else {
            onMobileData = false;
            Log.i("ConnectivityHandler", "No cellular data was being used.");
        }
    }

    fun reconnectToPreviousNetwork() {
        connectivityManager.bindProcessToNetwork(null)
        Log.i("ConnectivityManager", "Bound to null network.");
    }

    /**
     * Registers a network callback to monitor for disconnection if the hotspot goes offline.
     */
    private fun registerNetworkCallback(networkRequest: NetworkRequest, onConnected: () -> Unit, onConnectFailure: () -> Unit) {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                connectivityManager.bindProcessToNetwork(network);
                Log.i("ConnectivityHandler", "Connected to camera hotspot!");

                onConnected()
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Log.e("ConnectivityHandler", "The specified camera hotspot is not available.");
                unregisterNetworkCallback()

                onConnectFailure()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.e("ConnectivityHandler", "The camera hotspot connection was lost!");
                reconnectToPreviousNetwork();
            }
        }

        connectivityManager.requestNetwork(networkRequest, networkCallback!!);
    }

    /**
     * Unregisters a registered network callback :3
     */
    private fun unregisterNetworkCallback() {
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it);
        }
        connectivityManager.bindProcessToNetwork(null);
    }
}