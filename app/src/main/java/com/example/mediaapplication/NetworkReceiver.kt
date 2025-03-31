package com.example.mediaapplication


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkReceiver : BroadcastReceiver() {

    companion object {
        private val _isConnected = MutableLiveData<Boolean>()
        val isConnected: LiveData<Boolean> get() = _isConnected

        fun checkCurrentNetworkStatus(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            return activeNetwork?.isConnectedOrConnecting == true
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        var isConnected = false

        if (action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
            val activeNetwork: NetworkInfo? =
                intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)
            if (activeNetwork != null) {
                isConnected = activeNetwork.isConnected
            }
        } else if (action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            isConnected = activeNetwork?.isConnected == true
        }

        _isConnected.postValue(isConnected) // Update LiveData with the new network status
    }
}

