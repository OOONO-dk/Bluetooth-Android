package com.example.bluetoothframework.control.controller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import com.example.bluetoothframework.connection.connector.BluetoothConnectorInterface
import com.example.bluetoothframework.control.advertising_timeout.DeviceAdvertisementTimeoutInterface
import com.example.bluetoothframework.control.delegates.BluetoothDeviceDelegate
import com.example.bluetoothframework.model.data.BluetoothDeviceInfo
import com.example.bluetoothframework.model.data.BluetoothScannerConfig
import com.example.bluetoothframework.model.enums.ConnectionState
import com.example.bluetoothframework.scanning.scanner.BluetoothScannerInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class BluetoothController @Inject constructor(
    private val bluetoothScanner: BluetoothScannerInterface,
    private val bluetoothConnector: BluetoothConnectorInterface,
    private val deviceAdvertisementTimeout: DeviceAdvertisementTimeoutInterface
) : BluetoothControllerInterface {
    private var bluetoothDeviceDelegate: BluetoothDeviceDelegate? = null
    private val _devices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())

    init {
        bluetoothScanner.setScanDelegate(this)
        bluetoothConnector.setConnectionDelegate(this)
    }

    override fun setBluetoothDeviceListener(listener: BluetoothDeviceDelegate) {
        bluetoothDeviceDelegate = listener
    }


    /**
     * Listeners
     */
    override fun onDeviceDiscovered(scanResult: ScanResult) {
        onScanResult(scanResult)
    }

    override fun onScanFailed() {
        bluetoothDeviceDelegate?.onScanFailed()
        deviceAdvertisementTimeout.stopDeviceDiscoverTimeoutCheck()

    }

    override fun onDeviceConnected(gatt: BluetoothGatt) {
        bluetoothDeviceDelegate?.onDeviceConnected(gatt)
    }

    override fun onDeviceDisconnected(gatt: BluetoothGatt) {
        bluetoothDeviceDelegate?.onDeviceDisconnected(gatt)
    }

    override fun onConnectionFail(gatt: BluetoothGatt) {
        bluetoothDeviceDelegate?.onConnectionFail(gatt)
    }

    override fun onCharacteristicChanged(
        byteArray: ByteArray,
        gatt: BluetoothGatt,
        characteristicUuid: String
    ) {
        bluetoothDeviceDelegate?.onCharacteristicChanged(byteArray, gatt, characteristicUuid)
    }


    /**
     * Bluetooth controls
     */
    override fun startDiscovery(scannerConfig: BluetoothScannerConfig) {
        bluetoothScanner.startDiscovery(scannerConfig)
        deviceAdvertisementTimeout.checkDeviceDiscoverTimeout(scannerConfig.advertisingCheckIntervalMillis) {
            removeOldDevices(scannerConfig.advertisingExpirationIntervalMillis)
        }
    }

    override fun stopDiscovery() {
        bluetoothScanner.stopDiscovery()
        deviceAdvertisementTimeout.stopDeviceDiscoverTimeoutCheck()
    }

    override fun connectDevice(device: BluetoothDevice) {
        bluetoothConnector.connectDevice(device)
    }

    override fun disconnectDevice(device: BluetoothDevice) {
        bluetoothConnector.disconnectDevice(device)
    }

    override fun writeToDevice(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        payload: ByteArray
    ) {
        bluetoothConnector.writeToDevice(gatt, characteristic, payload)
    }


    /**
     * Scan functions
     */
    private fun onScanResult(result: ScanResult) {
        val newDevice = BluetoothDeviceInfo(
            device = result.device,
            lastSeen = System.currentTimeMillis(),
            rssi = result.rssi,
            connectionState = ConnectionState.DISCOVERED
        )

        val updatedList = updateDeviceInfo(result)

        _devices.update { devices ->
            val deviceExists = updatedList != devices
            if (deviceExists) updatedList else {
                bluetoothDeviceDelegate?.onDeviceDiscovered(result.device)
                devices + newDevice
            }
        }
    }

    private fun updateDeviceInfo(result: ScanResult): List<BluetoothDeviceInfo> {
        return _devices.value.map { bluetoothDeviceInfo ->
            val device = bluetoothDeviceInfo.device
            if (device.address == result.device.address) {
                bluetoothDeviceInfo.copy(
                    lastSeen = System.currentTimeMillis(),
                    rssi = result.rssi
                )
            } else {
                bluetoothDeviceInfo
            }
        }
    }

    private fun removeOldDevices(discoverInactivityDurationMillis: Long) {
        val currentTime = System.currentTimeMillis()
        val thresholdTime = currentTime - discoverInactivityDurationMillis

        val updatedList = _devices.value.filter {
            it.lastSeen > thresholdTime
        }

        val removedDevices =  _devices.value - updatedList.toSet()
        removedDevices.forEach { bluetoothDeviceDelegate?.onDeviceRemoved(it.device.address) }

        _devices.value = updatedList
    }


    /**
     * Connect functions
     */
}