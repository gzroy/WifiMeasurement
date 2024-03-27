package com.example.wifimeasurement

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class WifiMeasureViewModel  : ViewModel() {
    var positionName by mutableStateOf("")
        private set

    fun updatePositionName(name: String) {
        positionName = name
    }
}