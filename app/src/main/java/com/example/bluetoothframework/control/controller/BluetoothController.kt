package com.example.bluetoothframework.control.controller

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import com.example.bluetoothframework.connection.connector.BluetoothConnectorInterface
import com.example.bluetoothframework.control.advertising_timeout.DeviceAdvertisementTimeoutInterface
import com.example.bluetoothframework.control.delegates.BluetoothDeviceDelegate
import com.example.bluetoothframework.extensions.toBluetoothDeviceInfo
import com.example.bluetoothframework.model.data.BluetoothConnectData
import com.example.bluetoothframework.model.data.BluetoothDeviceInfo
import com.example.bluetoothframework.model.data.BluetoothScannerConfig
import com.example.bluetoothframework.model.data.BluetoothWriteData
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
        setGattForDevice(gatt)?.let {
            updateConnectionState(it, ConnectionState.CONNECTED)
        }

        //TODO discover services logic
    }

    override fun onDeviceDisconnected(gatt: BluetoothGatt) {
        getDeviceFromGatt(gatt)?.let {
            removeFromDevices(it)
            bluetoothDeviceDelegate?.onDeviceDisconnected(it)
        }
    }

    override fun onConnectionFail(gatt: BluetoothGatt) {
        getDeviceFromGatt(gatt)?.let {
            removeFromDevices(it)
            bluetoothDeviceDelegate?.onConnectionFail(it)
        }
    }

    override fun onCharacteristicChanged(
        byteArray: ByteArray,
        gatt: BluetoothGatt,
        characteristicUuid: String
    ) {
        getDeviceFromGatt(gatt)?.let {
            bluetoothDeviceDelegate?.onCharacteristicChanged(byteArray, it, characteristicUuid)
        }
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

    override fun connectDevices(devices: List<BluetoothConnectData>) {
        for (data in devices) {
            updateConnectionState(data.device, ConnectionState.CONNECTING)
            bluetoothConnector.connectDevice(data.device.device)
        }
    }

    override fun disconnectDevices(devices: List<BluetoothDeviceInfo>) {
        for (device in devices) {
            updateConnectionState(device, ConnectionState.DISCONNECTING)
            bluetoothConnector.disconnectDevice(device.device)
        }
    }

    override fun writeToDevices(devices: List<BluetoothWriteData>) {
        //bluetoothConnector.writeToDevice(gatt, characteristic, payload)
    }


    /**
     * Scan functions
     */
    private fun onScanResult(result: ScanResult) {
        val newDevice = result.toBluetoothDeviceInfo()
        val updatedList = updateDeviceInfo(result)

        _devices.update { devices ->
            val deviceExists = updatedList != devices

            if (!deviceExists) {
                bluetoothDeviceDelegate?.onDeviceDiscovered(newDevice)
                devices + newDevice
            } else {
                updatedList
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
        removedDevices.forEach { bluetoothDeviceDelegate?.onDeviceStoppedAdvertising(it) }

        _devices.value = updatedList
    }


    /**
     * Connect functions
     */
    // if services empty, discover services
    // if services not empty, create new list
    // if services has not characteristcs, discover characteristics
    // if services has characteristics, use the listed list
    // Convert the string uuids to real stuff
    private fun getDeviceFromGatt(gatt: BluetoothGatt): BluetoothDeviceInfo? {
        return _devices.value.firstOrNull { it.device.address == gatt.device.address }
    }

    private fun updateConnectionState(device: BluetoothDeviceInfo, connectionState: ConnectionState) {
        _devices.update { devices ->
            devices.map {
                if (it.device.address == device.device.address) {
                    bluetoothDeviceDelegate?.onConnectionStateUpdate(device, connectionState)
                    it.copy(connectionState = connectionState)
                } else {
                    it
                }
            }
        }

    }

    private fun removeFromDevices(device: BluetoothDeviceInfo) {
        _devices.update { devices ->
            devices.filter { it.device.address != device.device.address }
        }
    }

    private fun setGattForDevice(gatt: BluetoothGatt): BluetoothDeviceInfo? {
        var device: BluetoothDeviceInfo? = null
        _devices.update { devices ->
            devices.map {
                if (it.device.address == gatt.device.address) {
                    val updatedDevice = it.copy(gatt = gatt)
                    device = updatedDevice
                    updatedDevice
                } else {
                    it
                }
            }
        }
        return device
    }
}