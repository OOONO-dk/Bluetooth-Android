package com.example.bluetoothframework.presentation

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
        _state
    ) { scannedDevices, state ->
        state.copy(scannedDevices = scannedDevices)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value) //TODO: need this?

    fun startScan() {
        bluetoothHelper.startDiscovery()
    }

    fun stopScan() {
        bluetoothHelper.stopDiscovery()
    }
}