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
import com.example.bluetoothframework.model.data.BluetoothService
import com.example.bluetoothframework.model.data.BluetoothWriteData
import com.example.bluetoothframework.model.enums.ConnectionState
import com.example.bluetoothframework.scanning.scanner.BluetoothScannerInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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


    /**************************************
    *            Listeners Start
     *************************************/
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
            bluetoothConnector.discoverServices(gatt)
        }
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

    override fun onServicesDiscovered(gatt: BluetoothGatt) {
        getDeviceFromGatt(gatt)?.let { setServicesNotifiers(it) }
    }
    /**************************************
     *            Listeners End
     *************************************/






    /**************************************
     *      Bluetooth Controls Start
     *************************************/
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
            if (deviceIsConnected(data.device)) continue
            updateConnectionState(data.device, ConnectionState.CONNECTING)
            bluetoothConnector.connectDevice(data.device.device)
            if (data.services.isNotEmpty()) setServicesForDevice(data)
        }
    }

    override fun disconnectDevices(devices: List<BluetoothDeviceInfo>) {
        for (device in devices) {
            updateConnectionState(device, ConnectionState.DISCONNECTING)
            getDeviceGatt(device)?.let { bluetoothConnector.disconnectDevice(it) }
        }
    }

    override fun writeToDevices(devices: List<BluetoothWriteData>) {
        for (data in devices) {
            getDeviceGatt(data.device)?.let { gatt ->
                val characteristic = bluetoothConnector.getCharacteristicFromUuid(gatt, data.serviceUuid, data.characteristicUuid)
                characteristic?.let { bluetoothConnector.writeToDevice(gatt, it, data.payload) }
            }
        }
    }
    /**************************************
     *      Bluetooth Controls End
     *************************************/






    /**************************************
     *        Scan Functions Start
     *************************************/
    private fun onScanResult(result: ScanResult) {
        val device = result.toBluetoothDeviceInfo()
        val updatedList = updateDeviceInfo(result)
        addOrUpdatedDiscoveredDevice(device, updatedList)
    }

    private fun addOrUpdatedDiscoveredDevice(device: BluetoothDeviceInfo, updatedList: List<BluetoothDeviceInfo>) {
        _devices.update { devices ->
            val deviceExists = devices.any { it.device.address == device.device.address }

            if (!deviceExists) {
                bluetoothDeviceDelegate?.onDeviceDiscovered(device)
                devices + device
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

        val oldDevices = _devices.value.filter {
            it.connectionState == ConnectionState.DISCOVERED && it.lastSeen < thresholdTime
        }

        oldDevices.forEach {
            bluetoothDeviceDelegate?.onDeviceStoppedAdvertising(it)
            removeFromDevices(it)
        }
    }
    /**************************************
     *        Scan Functions End
     *************************************/






    /**************************************
     *     Connection Functions Start
     *************************************/
    private fun setServicesNotifiers(device: BluetoothDeviceInfo) {
        val gatt = device.gatt ?: return

        if (device.services.isEmpty()) {
            bluetoothConnector.setServiceNotifiers(gatt)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            for (service in device.services) {
                if (service.characteristics.isEmpty()) {
                    discoverAndEnableAllCharacteristics(gatt, service.serviceUuid)
                } else {
                    enableCharacteristics(gatt, service)
                }
            }
        }
    }

    private suspend fun discoverAndEnableAllCharacteristics(gatt: BluetoothGatt, serviceUuid: String) {
        val characteristics = bluetoothConnector.getAllCharacteristics(gatt, serviceUuid)
        for (characteristic in characteristics) {
            bluetoothConnector.enableNotifications(gatt, characteristic)
        }
    }

    private suspend fun enableCharacteristics(gatt: BluetoothGatt, bluetoothService: BluetoothService) {
        for (characteristicUuid in bluetoothService.characteristics) {
            bluetoothConnector.getCharacteristicFromUuid(gatt, bluetoothService.serviceUuid, characteristicUuid)?.let {
                bluetoothConnector.enableNotifications(gatt, it)
            }
        }
    }
    /**************************************
     *     Connection Functions End
     *************************************/






    /**************************************
     *        List Functions Start
     *************************************/
    private fun getDeviceGatt(device: BluetoothDeviceInfo): BluetoothGatt? {
        return _devices.value.firstOrNull { it.device.address == device.device.address }?.gatt
    }

    private fun deviceIsConnected(device: BluetoothDeviceInfo): Boolean {
        return _devices.value.any {
            it.device.address == device.device.address &&
            it.connectionState == ConnectionState.CONNECTED
        }
    }

    private fun removeFromDevices(device: BluetoothDeviceInfo) {
        _devices.update { devices ->
            devices.filter { it.device.address != device.device.address }
        }
    }

    private fun getDeviceFromGatt(gatt: BluetoothGatt): BluetoothDeviceInfo? {
        return _devices.value.firstOrNull { it.device.address == gatt.device.address }
    }

    private fun setServicesForDevice(data: BluetoothConnectData) {
        modifyDeviceInfo(data.device.device.address) {
            it.copy(services = data.services)
        }
    }

    private fun setGattForDevice(gatt: BluetoothGatt): BluetoothDeviceInfo? {
        return modifyDeviceInfo(gatt.device.address) { it.copy(gatt = gatt) }
    }

    private fun updateConnectionState(device: BluetoothDeviceInfo, connectionState: ConnectionState) {
        modifyDeviceInfo(device.device.address) {
            bluetoothDeviceDelegate?.onConnectionStateUpdate(device, connectionState)
            it.copy(connectionState = connectionState)
        }
    }

    private inline fun modifyDeviceInfo(
        address: String,
        operation: (BluetoothDeviceInfo) -> BluetoothDeviceInfo
    ): BluetoothDeviceInfo? {
        var modifiedDevice: BluetoothDeviceInfo? = null
        _devices.update { devices ->
            devices.map {
                if (it.device.address == address) {
                    val updatedDevice = operation(it)
                    modifiedDevice = updatedDevice
                    updatedDevice
                } else {
                    it
                }
            }
        }
        return modifiedDevice
    }
    /**************************************
     *        List Functions End
     *************************************/
}