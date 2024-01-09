package com.example.bluetoothframework.control.controller

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import com.example.bluetoothframework.connection.connector.BluetoothConnector
import com.example.bluetoothframework.connection.delegates.BluetoothConnectForwarderDelegate
import com.example.bluetoothframework.control.advertising_timeout.AdvertisementTimeout
import com.example.bluetoothframework.control.delegates.BluetoothDeviceDelegate
import com.example.bluetoothframework.control.utils.DevicesListHelper
import com.example.bluetoothframework.extensions.toBluetoothDeviceInfo
import com.example.bluetoothframework.model.data.BluetoothConnectData
import com.example.bluetoothframework.model.data.BluetoothDeviceInfo
import com.example.bluetoothframework.model.data.BluetoothScannerConfig
import com.example.bluetoothframework.model.data.BluetoothService
import com.example.bluetoothframework.model.data.BluetoothWriteData
import com.example.bluetoothframework.model.enums.ConnectionState
import com.example.bluetoothframework.scanning.delegates.BluetoothScanForwarderDelegate
import com.example.bluetoothframework.scanning.scanner.BluetoothScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class BluetoothControllerImpl @Inject constructor(
    private val listHelper: DevicesListHelper,
    private val bluetoothScanner: BluetoothScanner,
    private val bluetoothConnector: BluetoothConnector,
    private val deviceAdvertisementTimeout: AdvertisementTimeout
) : BluetoothController,
    BluetoothScanForwarderDelegate,
    BluetoothConnectForwarderDelegate {
    private var bluetoothDeviceDelegate: BluetoothDeviceDelegate? = null
    private val deviceList = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())

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
        deviceList.value = listHelper.setGattForDevice(deviceList, gatt)
        listHelper.getDeviceFromGatt(deviceList.value, gatt)?.let {
            deviceList.value = listHelper.updateConnectionState(
                list = deviceList,
                device = it,
                delegate = bluetoothDeviceDelegate,
                connectionState = ConnectionState.CONNECTED
            )
            bluetoothConnector.discoverServices(gatt)
        }
    }

    override fun onDeviceDisconnected(gatt: BluetoothGatt) {
        listHelper.getDeviceFromGatt(deviceList.value, gatt)?.let {
            deviceList.value = listHelper.removeFromDevices(deviceList.value, it)
            bluetoothDeviceDelegate?.onDeviceDisconnected(it)
        }
    }

    override fun onConnectionFail(gatt: BluetoothGatt) {
        listHelper.getDeviceFromGatt(deviceList.value, gatt)?.let {
            deviceList.value = listHelper.removeFromDevices(deviceList.value, it)
            bluetoothDeviceDelegate?.onConnectionFail(it)
        }
    }

    override fun onCharacteristicChanged(
        byteArray: ByteArray,
        gatt: BluetoothGatt,
        characteristicUuid: String
    ) {
        listHelper.getDeviceFromGatt(deviceList.value, gatt)?.let {
            bluetoothDeviceDelegate?.onCharacteristicChanged(byteArray, it, characteristicUuid)
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt) {
        listHelper.getDeviceFromGatt(deviceList.value, gatt)?.let { setServicesNotifiers(it) }
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

            if (listHelper.deviceIsConnected(deviceList.value, data.device)) {
                continue
            }

            deviceList.value = listHelper.updateConnectionState(
                list = deviceList,
                device = data.device,
                delegate = bluetoothDeviceDelegate,
                connectionState = ConnectionState.CONNECTING
            )

            bluetoothConnector.connectDevice(data.device.device)
            if (data.services.isNotEmpty()) {
                deviceList.value = listHelper.setServicesForDevice(deviceList, data)
            }
        }
    }

    override fun disconnectDevices(devices: List<BluetoothDeviceInfo>) {
        for (device in devices) {

            deviceList.value = listHelper.updateConnectionState(
                list = deviceList,
                device = device,
                delegate = bluetoothDeviceDelegate,
                connectionState = ConnectionState.DISCONNECTING
            )

            listHelper.getDeviceGatt(deviceList.value, device)
                ?.let { bluetoothConnector.disconnectDevice(it) }
        }
    }

    override fun writeToDevices(devices: List<BluetoothWriteData>) {
        for (data in devices) {
            listHelper.getDeviceGatt(deviceList.value, data.device)?.let { gatt ->
                val characteristic = bluetoothConnector.getCharacteristicFromUuid(
                    gatt,
                    data.serviceUuid,
                    data.characteristicUuid
                )
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

    private fun addOrUpdatedDiscoveredDevice(
        device: BluetoothDeviceInfo,
        updatedList: List<BluetoothDeviceInfo>
    ) {
        deviceList.update { devices ->
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
        return deviceList.value.map { bluetoothDeviceInfo ->
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

        val oldDevices = deviceList.value.filter {
            it.connectionState == ConnectionState.DISCOVERED && it.lastSeen < thresholdTime
        }

        oldDevices.forEach {
            bluetoothDeviceDelegate?.onDeviceStoppedAdvertising(it)
            deviceList.value = listHelper.removeFromDevices(deviceList.value, it)
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

    private suspend fun discoverAndEnableAllCharacteristics(
        gatt: BluetoothGatt,
        serviceUuid: String
    ) {
        val characteristics = bluetoothConnector.getAllCharacteristics(gatt, serviceUuid)
        for (characteristic in characteristics) {
            bluetoothConnector.enableNotifications(gatt, characteristic)
        }
    }

    private suspend fun enableCharacteristics(
        gatt: BluetoothGatt,
        bluetoothService: BluetoothService
    ) {
        for (characteristicUuid in bluetoothService.characteristics) {
            bluetoothConnector.getCharacteristicFromUuid(
                gatt,
                bluetoothService.serviceUuid,
                characteristicUuid
            )?.let {
                bluetoothConnector.enableNotifications(gatt, it)
            }
        }
    }
    /**************************************
     *     Connection Functions End
     *************************************/
}