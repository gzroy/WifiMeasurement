package com.example.wifimeasurement

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.channels.Channel

class WifiScanService(context: Context) {
    private val wifiManager by lazy {
        context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val context: Context = context
    val data: Channel<List<WifiMeasureData>> = Channel(Channel.UNLIMITED)

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }

    fun init() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)

        val success = wifiManager.startScan()
        if (!success) {
            scanFailure()
        }
    }

    private fun scanFailure() {
        Log.d("WIFI", "Scan failure")
    }

    @SuppressLint("MissingPermission")
    private fun scanSuccess() {
        val results = wifiManager.scanResults
        if (!results.isNullOrEmpty()) {
            val wifiMeasureData = results.map {
                WifiMeasureData(
                    it.BSSID,
                    it.level
                )
            }
            data.trySend(wifiMeasureData)
        }
    }

    fun cancel() {
        context.unregisterReceiver(wifiScanReceiver)
    }
}