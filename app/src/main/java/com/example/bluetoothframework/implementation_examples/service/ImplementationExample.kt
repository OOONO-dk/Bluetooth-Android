package com.example.bluetoothframework.implementation_examples.service

import android.annotation.SuppressLint
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import com.example.bluetoothframework.control.controller.BluetoothControllerInterface
import com.example.bluetoothframework.model.data.BluetoothConnectData
import com.example.bluetoothframework.model.data.BluetoothDeviceInfo
import com.example.bluetoothframework.model.data.BluetoothScannerConfig
import com.example.bluetoothframework.model.data.BluetoothWriteData
import com.example.bluetoothframework.model.enums.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ImplementationExample @Inject constructor(
    private val bluetoothController: BluetoothControllerInterface
) : ImplementationExampleInterface {
    private val _devices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())

    override val devices: StateFlow<List<BluetoothDeviceInfo>>
        get() = _devices.asStateFlow()

    init {
        bluetoothController.setBluetoothDeviceListener(this)
    }

    private val serviceUUIDs = listOf(
        // Sirene
        "5E631523-6743-11ED-9022-0242AC120002",

        // Co-driver
        "00001523-1212-efde-1523-785feabcd123",
        "0ef51523-d3f3-4e34-93de-8bcac38fc718",
        "0b40dfe0-7c04-43ae-a07a-aa28c52e9328",
        "72e81523-d54b-48ea-a00c-5ea3973cebc7"
    )

    private val scanFilter = serviceUUIDs.map { serviceUuid ->
        ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(serviceUuid)).build()
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanConfig = BluetoothScannerConfig(
        scanFilter = scanFilter,
        scanSettings = scanSettings,
        advertisingCheckIntervalMillis = TimeUnit.SECONDS.toMillis(1),
        advertisingExpirationIntervalMillis = TimeUnit.SECONDS.toMillis(5)
    )

    override fun startDiscovery() {
        bluetoothController.startDiscovery(scanConfig)
    }

    override fun stopDiscovery() {
        bluetoothController.stopDiscovery()
    }

    override fun connectDevices(devices: List<BluetoothConnectData>) {
        bluetoothController.connectDevices(devices)
    }

    override fun disconnectDevices(devices: List<BluetoothDeviceInfo>) {
        bluetoothController.disconnectDevices(devices)
    }

    override fun writeToDevices(devices: List<BluetoothWriteData>) {
        bluetoothController.writeToDevices(devices)
    }


    override fun onConnectionStateUpdate(device: BluetoothDeviceInfo, newState: ConnectionState) {
        updateDeviceState(device, newState)
    }

    /***************************
     * Delegates.
     **************************/
    override fun onDeviceDiscovered(device: BluetoothDeviceInfo) {
        _devices.update { devices ->
            devices + device
        }
    }

    override fun onScanFailed() {
        startDiscovery()
    }

    override fun onDeviceDisconnected(device: BluetoothDeviceInfo) {
        removeDevice(device)
    }

    override fun onConnectionFail(device: BluetoothDeviceInfo) {
        removeDevice(device)
    }

    override fun onDeviceStoppedAdvertising(device: BluetoothDeviceInfo) {
        removeDevice(device)
    }

    private fun removeDevice(device: BluetoothDeviceInfo) {
        _devices.update { devices ->
            devices.filter {
                it.device.address != device.device.address
            }
        }
    }

    private fun updateDeviceState(device: BluetoothDeviceInfo, newState: ConnectionState) {
        _devices.update { devices ->
            devices.map {
                if (it.device.address == device.device.address) {
                    it.copy(connectionState = newState)
                } else {
                    it
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCharacteristicChanged(byteArray: ByteArray, device: BluetoothDeviceInfo, characteristicUuid: String) {
        //println("RRR - onCharacteristicChanged: ${device.address} - ${characteristicUuid}")
    }
}