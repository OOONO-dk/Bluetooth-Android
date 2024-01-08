package com.example.bluetoothframework.implementation_examples.presentation

import androidx.lifecycle.ViewModel
import com.example.bluetoothframework.implementation_examples.service.ImplementationExampleInterface
import com.example.bluetoothframework.model.data.BluetoothConnectData
import com.example.bluetoothframework.model.data.BluetoothDeviceInfo
import com.example.bluetoothframework.model.data.BluetoothService
import com.example.bluetoothframework.model.data.BluetoothWriteData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bluetoothHelper: ImplementationExampleInterface
): ViewModel() {
    private val _state = bluetoothHelper.devices
    val state: StateFlow<List<BluetoothDeviceInfo>> = _state

    fun startScan() {
        bluetoothHelper.startDiscovery()
    }

    fun stopScan() {
        bluetoothHelper.stopDiscovery()
    }

    fun connectToDevice(device: BluetoothDeviceInfo) {
        bluetoothHelper.connectDevices(listOf(BluetoothConnectData(
            device = device,
            /*listOf(
                BluetoothService(
                    serviceUuid = "5e63f720-6743-11ed-9022-0242ac120002",
                    characteristics = listOf(
                        "5e63f722-6743-11ed-9022-0242ac120002"
                    )
                )
            )*/
        )))
    }

    fun disconnectDevice(device: BluetoothDeviceInfo) {
        bluetoothHelper.disconnectDevices(listOf(device))
    }

    private fun writeToDevice(device: BluetoothDeviceInfo, payload: ByteArray) {
        val serviceUuid = "5e63f720-6743-11ed-9022-0242ac120002"
        val characteristicUuid = "5e63f721-6743-11ed-9022-0242ac120002"

        bluetoothHelper.writeToDevices(
            listOf(
                BluetoothWriteData(
                    device = device,
                    serviceUuid = serviceUuid,
                    characteristicUuid = characteristicUuid,
                    payload = payload
                )
            )
        )
    }

    fun blinkSirene(device: BluetoothDeviceInfo) {
        writeToDevice(device, byteArrayOf(0x02, 0x0E, 0x00, 0x0C, 0x0F, 0x0C, 0x00, 0x0C, 0x0F, 0x0C, 0x00, 0x0C, 0x0F, 0x0C, 0x00, 0x0C))
        writeToDevice(device, byteArrayOf(0x04, 0x01, 0x01))
        writeToDevice(device, byteArrayOf(0x02, 0x00))
    }
}