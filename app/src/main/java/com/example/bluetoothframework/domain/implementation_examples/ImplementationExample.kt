package com.example.bluetoothframework.domain.implementation_examples

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import com.example.bluetoothframework.domain.controller.BluetoothControllerInterface
import com.example.bluetoothframework.domain.scan.BluetoothScannerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ImplementationExample @Inject constructor(
    private val bluetoothController: BluetoothControllerInterface
) : ImplementationExampleInterface {
    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    private val _connectedDevices = MutableStateFlow<List<BluetoothGatt>>(emptyList())

    override val scannedDevices: StateFlow<List<BluetoothDevice>>
        get() = _scannedDevices.asStateFlow()
    override val connectedDevices: StateFlow<List<BluetoothGatt>>
        get() = _connectedDevices.asStateFlow()

    init {
        bluetoothController.setScanCallback(this)
        bluetoothController.setConnectCallback(this)
        bluetoothController.setCharacteristicChangeCallback(this)
    }

    private val serviceUUIDs = listOf(
        // Sirene
        "5E631523-6743-11ED-9022-0242AC120002",
        //"5E630000-6743-11ED-9022-0242AC120002",
        //"5E63F720-6743-11ED-9022-0242AC120002",
        //"5E63F721-6743-11ED-9022-0242AC120002",
        //"5E63F722-6743-11ED-9022-0242AC120002",

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

    override fun connectDevice(device: BluetoothDevice) {
        bluetoothController.connectDevice(device)
    }

    override fun disconnectDevice(device: BluetoothDevice) {
        bluetoothController.disconnectDevice(device)
    }

    override fun writeToDevice(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        bluetoothController.writeToDevice(gatt, characteristic, payload)
    }

    /***************************
     * Delegates.
     **************************/
    override fun onDeviceDiscovered(newDevice: BluetoothDevice) {
        _scannedDevices.update { scannedDevices ->
            scannedDevices + newDevice
        }
    }

    override fun onDeviceRemoved(removedDeviceAddress: String) {
        _scannedDevices.update {
            _scannedDevices.value.filter {
                it.address != removedDeviceAddress
            }
        }
    }

    override fun onScanFailed() {
        startDiscovery()
    }

    override fun onDeviceConnected(gatt: BluetoothGatt) {
        println("RRR - onDeviceConnected: ${gatt.device.address}")
        _connectedDevices.update { connectedDevices ->
            connectedDevices + gatt
        }
    }

    override fun onDeviceDisconnected(gatt: BluetoothGatt) {
        println("RRR - onDeviceDisconnected: ${gatt.device.address}")
        _connectedDevices.update {
            _connectedDevices.value.filter {
                it.device.address != gatt.device.address
            }
        }
    }

    override fun onConnectionFail(gatt: BluetoothGatt) {
        println("RRR - onConnectionFail: ${gatt.device.address}")
        _connectedDevices.update {
            _connectedDevices.value.filter {
                it.device.address != gatt.device.address
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCharacteristicChanged(byteArray: ByteArray, device: BluetoothDevice, characteristicUuid: String) {
    }
}