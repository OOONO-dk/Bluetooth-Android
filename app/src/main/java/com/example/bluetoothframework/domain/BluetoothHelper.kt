package com.example.bluetoothframework.domain

import com.example.bluetoothframework.domain.scanner.GG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class BluetoothHelper @Inject constructor() : GG {
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    override fun onDeviceRemoved(removedDevice: BluetoothDeviceDomain) {
        println("RRR - removed: ${removedDevice.address}")
        _scannedDevices.update {
            _scannedDevices.value.filter {
                it.address != removedDevice.address
            }
        }
    }

    override fun onDeviceDiscovered(newDevice: BluetoothDeviceDomain) {
        println("RRR - added: ${newDevice.address}")
        _scannedDevices.update { scannedDevices ->
            scannedDevices + newDevice
        }
    }
}