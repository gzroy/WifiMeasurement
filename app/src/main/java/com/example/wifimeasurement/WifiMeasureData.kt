package com.example.wifimeasurement

data class WifiMeasureData (
    val bssId: String,
    val signalStrength: Int
)

data class WifiMeasureReportData (
    val position: String,
    val angle: Float,
    val data: List<WifiMeasureData>
)