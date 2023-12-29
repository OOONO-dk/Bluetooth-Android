package com.example.bluetoothframework.presentation

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothframework.domain.extensions.toUuid
import com.example.bluetoothframework.domain.implementation_examples.ImplementationExampleInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bluetoothHelper: ImplementationExampleInterface
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

    fun disconnectDevice(device: BluetoothDevice) {
        bluetoothHelper.disconnectDevice(device)
    }

    private fun writeToDevice(gatt: BluetoothGatt, payload: ByteArray) {
        val serviceUuid = "5e63f720-6743-11ed-9022-0242ac120002".toUuid()
        val characteristicUuid = "5e63f721-6743-11ed-9022-0242ac120002".toUuid()

        gatt.getService(serviceUuid)?.getCharacteristic(characteristicUuid)?.let {
            bluetoothHelper.writeToDevice(gatt, it, payload)
        }
    }

    fun blinkSirene(gatt: BluetoothGatt) {
        writeToDevice(gatt, byteArrayOf(0x02, 0x0E, 0x00, 0x0C, 0x0F, 0x0C, 0x00, 0x0C, 0x0F, 0x0C, 0x00, 0x0C, 0x0F, 0x0C, 0x00, 0x0C))
        writeToDevice(gatt, byteArrayOf(0x04, 0x01, 0x01))
        writeToDevice(gatt, byteArrayOf(0x02, 0x00))

        /*viewModelScope.launch {
            writeToDevice(gatt, byteArrayOf(0x02, 0x0E, 0x00, 0x0C, 0x0F, 0x0C, 0x00, 0x0C, 0x0F, 0x0C, 0x00, 0x0C, 0x0F, 0x0C, 0x00, 0x0C))
            delay(1000)
            writeToDevice(gatt, byteArrayOf(0x04, 0x01, 0x01))
            delay(1000)
            writeToDevice(gatt, byteArrayOf(0x02, 0x00))
        }*/
    }
}