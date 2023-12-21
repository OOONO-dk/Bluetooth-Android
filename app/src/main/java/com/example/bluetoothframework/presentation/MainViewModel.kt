package com.example.bluetoothframework.presentation

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothframework.domain.implementation_examples.ScannerExampleInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bluetoothHelper: ScannerExampleInterface
): ViewModel() {
    private val _state = MutableStateFlow(BluetoothUiState())
    val state = combine(
        bluetoothHelper.scannedDevices,
        bluetoothHelper.connectedDevices,
        _state
    ) { scannedDevices, connectedDevices, state ->
        state.copy(scannedDevices = scannedDevices, connectedDevices = connectedDevices)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    fun startScan() {
        bluetoothHelper.startDiscovery()
    }

    fun stopScan() {
        bluetoothHelper.stopDiscovery()
    }

    fun connectToDevice(device: BluetoothDevice) {
        bluetoothHelper.connectDevice(device)
    }
}